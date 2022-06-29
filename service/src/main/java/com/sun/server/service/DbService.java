package com.sun.server.service;

import com.sun.common.entity.Entity;
import com.sun.common.util.Result;
import com.sun.server.mapper.BatchMapper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description:
 * @author: Sun Xiaodong
 */
public interface DbService extends Service {


    /**
     * 批处理数据
     * @param clazz  list集合元素类型
     * @param list  批处理数据元素集合
     * @param batchType  批处理类型（插入、更新、删除）
     * @param <T>  泛型声明
     * @return  根据Result.result()结果判断是否成功
     *          当result()返回结果为false时，如果需要对失败结果做进一步处理，那么需要将getData()值强转为Object、再强转为List<Map<T, Integer>>后使用
     */
    <T extends Entity> Result<List<T>> batch(Class<T> clazz, List<T> list, final BatchMapper.BatchType batchType);

    void close();


    /**
     *  自定义线程名的固定线程数量的线程池
     *
     * @param poolSize  线程池固定的线程数量
     * @return  线程池
     */
    static ExecutorService initFixedThreadPool(int poolSize, final int taskSize, final String threadNamePrefix) {
        poolSize = poolSize <= 0 ? (Runtime.getRuntime().availableProcessors() + 1) : poolSize;
        return new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(taskSize),  new ThreadFactory() {
            private final AtomicInteger seqNo = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, threadNamePrefix.concat(String.valueOf(seqNo.getAndIncrement())));
            }}, new ThreadPoolExecutor.DiscardPolicy());
    }


    /**
     * 优雅关闭线程池资源
     * @param executor  线程池对象
     * @param time  等待时间
     * @param unit  时间单位
     */
    static void awaitGracefullyClose(ExecutorService executor, long time, TimeUnit unit) {
        if (null != executor && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(time, unit)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

}
