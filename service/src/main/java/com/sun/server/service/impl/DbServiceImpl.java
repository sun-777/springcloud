package com.sun.server.service.impl;

import com.sun.common.entity.Course;
import com.sun.common.entity.Entity;
import com.sun.common.entity.Schedule;
import com.sun.common.entity.Teacher;
import com.sun.common.util.Result;
import com.sun.server.mapper.BatchMapper;
import com.sun.server.mapper.CourseMapper;
import com.sun.server.mapper.ScheduleMapper;
import com.sun.server.mapper.TeacherMapper;
import com.sun.server.service.DbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.sun.common.util.Assertions.isTrueArgument;
import static com.sun.common.util.Assertions.notNull;
/**
 * @description:
 * @author: Sun Xiaodong
 */

@Service
public class DbServiceImpl implements DbService {
    private static final Logger LOG = LoggerFactory.getLogger(DbService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private TeacherMapper teacherMapper;
    private CourseMapper courseMapper;
    private ScheduleMapper scheduleMapper;

    private final ThreadPoolHolder executor = new ThreadPoolHolder();


    DbServiceImpl() {}


    @PostConstruct
    private void init() {
        this.teacherMapper = new TeacherMapper(this.entityManager);
        this.courseMapper = new CourseMapper(this.entityManager);
        this.scheduleMapper = new ScheduleMapper(this.entityManager);
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> Result<List<T>> batch(Class<T> clazz, final List<T> list, final BatchMapper.BatchType batchType) {
        if (null != clazz && null != list && !list.isEmpty()) {
            long start = System.nanoTime();
            final int total = list.size();
            BatchMapper<T> mapper = null;
            // ????????????????????????
            if (Teacher.class == clazz) {
                mapper = (BatchMapper<T>) notNull("", teacherMapper);
            } else if (Course.class == clazz) {
                mapper = (BatchMapper<T>) notNull("", courseMapper);
            } else if (Schedule.class == clazz) {
                mapper = (BatchMapper<T>) notNull("", scheduleMapper);
            }
            final int batchSize = notNull("batchSize", mapper).getBatchSize();
            final int tasks = getTaskNum(total, batchSize);
            final CountDownLatch latch = new CountDownLatch(tasks);
            ConcurrentLinkedQueue<Callable<Result<Map<T, Integer>>>> queue = sliceBatchTask(mapper, list, tasks, batchSize, batchType, latch);
            // ???????????????????????????????????????taskResultList
            List<Future<Result<Map<T, Integer>>>> taskResultList = new ArrayList<>(tasks);
            for (int i = 0; i < tasks; i++) {
                taskResultList.add(executor.executeTask(queue.poll()));
            }

            // ??????????????????????????????
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // ????????????????????????
            List<Map<T, Integer>> failedList = new ArrayList<>(0);
            taskResultList.forEach(future -> {
                if (future.isDone()) {
                    try {
                        final Result<Map<T, Integer>> result = future.get();
                        if (!result.result()) {
                            // ????????????????????????????????????????????????
                            failedList.add(result.getData());
                            LOG.error(result.getMessage());
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });

            if (failedList.isEmpty()) {
                // ???????????????????????????
                LOG.info("[`{}` table]: {} {} records???cost {}ms", clazz.getSimpleName().toLowerCase(Locale.ENGLISH), batchType.name().toLowerCase(Locale.ENGLISH), total, (System.nanoTime() - start) / 1000_000);
                final String successMsg = "Successful batch {0} of {1} records";
                return Result.success(list, new MessageFormat(successMsg).format(new Object[] {batchType.name().toLowerCase(Locale.ENGLISH), total}));
            } else {
                // ???????????????????????????????????????????????????????????????
                // ??????????????????Result.data???????????????Object???????????????List<Map<T, Integer>>??????????????????????????????
                List<T> data = (List<T>) ((Object) failedList);
                return Result.error(data);
            }
        }
        return Result.error();
    }


    //???????????????????????????????????????????????????????????????
    private int getTaskNum(final int total, final int batchSize) {
        final int tasks = (total / batchSize) + ((0 == total % batchSize) ? 0 : 1);
        isTrueArgument("greater than or equal to 1", tasks >= 1);
        return tasks;
    }


    //?????????????????????
    private <T extends Entity> ConcurrentLinkedQueue<Callable<Result<Map<T, Integer>>>> sliceBatchTask(
            final BatchMapper<T> mapper,
            final List<T> list,  // ??????????????????
            final int tasks, //?????????????????????????????????
            final int batchSize, // ?????????????????????
            final BatchMapper.BatchType batchType,  // ???????????????
            final CountDownLatch latch) {
        notNull("batchType", batchType);
        ConcurrentLinkedQueue<Callable<Result<Map<T, Integer>>>> queue = new ConcurrentLinkedQueue<>();
        if (1 == tasks) {
            queue.offer(newCallable(mapper, list, batchType, latch));
        } else {
            for (int i = 0; i < tasks; ) {
                List<T> sliceList = list.subList(i * batchSize, (i + 1) * batchSize);
                queue.offer(newCallable(mapper, sliceList, batchType, latch));
                // ????????????????????????????????????
                if (++i + 1 == tasks) {
                    final int total = list.size();
                    // ????????????????????????????????????????????????
                    int remain = total % batchSize;
                    if (0 == remain) {
                        remain = batchSize;
                    }
                    sliceList = list.subList(i * batchSize, i * batchSize + remain);
                    queue.offer(newCallable(mapper, sliceList, batchType, latch));
                    break;
                }
            }
        }
        return queue;
    }


    private <T extends Entity> Callable<Result<Map<T, Integer>>> newCallable(final BatchMapper<T> mapper, final List<T> list, final BatchMapper.BatchType batchType, final CountDownLatch latch) {
        return () -> {
            List<Integer> resultList = null;
            try {
                switch (batchType) {
                    case INSERT:
                        resultList = mapper.batchSave(list);
                        break;
                    case UPDATE:
                        resultList = mapper.batchUpdate(list);
                        break;
                    case DELETE:
                        resultList = mapper.batchDelete(list);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid batch type");
                }
                latch.countDown();
                return Result.success(encapsulateResultToMap(list, resultList));
            } catch (Exception e) {
                latch.countDown();
                return Result.error(null != resultList ? encapsulateResultToMap(list, resultList) : null, e);
            }
        };
    }


    // ?????????????????????????????????????????????Map
    private <T>  Map<T, Integer> encapsulateResultToMap(final List<T> entities, final List<Integer> results) {
        final int size = entities.size();
        isTrueArgument("entities size and results size must be equal", size == results.size());
        final Map<T, Integer> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(entities.get(i), results.get(i));
        }
        return map;
    }




    @PreDestroy
    public void close() {
        executor.close(3000, TimeUnit.SECONDS);
    }



    private static final class ThreadPoolHolder {
        private final Object LOCK = new Object();
        final String THREAD_NAME_PREFIX = this.toString().concat("_thread_");
        private static final int MAX_TASK_NUM = 10240;

        //?????????
        private volatile ExecutorService executors;

        ThreadPoolHolder() {}

        <T> Future<Result<T>> executeTask(Callable<Result<T>> task) {
            if (null == executors) {
                initThreadPool();
            }
            return executors.submit(task);
        }


        private void initThreadPool() {
            if (null == executors) {
                synchronized (LOCK) {
                    if (null == executors) {
                        executors = DbService.initFixedThreadPool(0, MAX_TASK_NUM, THREAD_NAME_PREFIX);
                    }
                }
            }
        }


        void close(long time, TimeUnit unit) {
            DbService.awaitGracefullyClose(executors, time, unit);
        }
    }
}
