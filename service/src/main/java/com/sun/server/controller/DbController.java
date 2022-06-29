package com.sun.server.controller;

import com.sun.common.entity.Course;
import com.sun.common.entity.Schedule;
import com.sun.common.entity.Teacher;
import com.sun.common.enumeration.Gender;
import com.sun.common.enumeration.Lesson;
import com.sun.common.enumeration.Weekday;
import com.sun.common.util.Result;
import com.sun.server.mapper.BatchMapper;
import com.sun.server.service.CourseService;
import com.sun.server.service.DbService;
import com.sun.server.service.ScheduleService;
import com.sun.server.service.TeacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.sun.common.util.Assertions.isTrueArgument;
import static com.sun.common.util.Assertions.notNull;
import static com.sun.server.controller.DbController.RecordGenerator.RANDOM;

/**
 * @description: 操作数据库表批量生成数据或者批量删除数据
 * @author: Sun Xiaodong
 */

@RestController
@RequestMapping("/db")
public class DbController implements BaseController {
    private static final Logger LOG = LoggerFactory.getLogger(DbController.class);
    @Resource
    private DbService dbService;
    @Resource
    private TeacherService teacherService;
    @Resource
    private CourseService courseService;
    @Resource
    private ScheduleService scheduleService;



    @PostMapping("/init")
    public Result<String> init() {

        // 初始化表记录之前，清除所有的表记录
        Result<String> clearResult = dbClear();
        // 清除所有表记录失败，则返回结果
        if (!clearResult.result()) {
            return clearResult;
        }

        /*=====================================================================*/
        // 初始化teacher表记录，假定生成1000条
        /*=====================================================================*/
        final int teacherRecordCount = 1000;
        Result<String> initTeacherResult = initTeacherTable(teacherRecordCount);
        // 初始化Teacher表记录失败，则返回结果
        if (!initTeacherResult.result()) {
            return initTeacherResult;
        }

        /*=====================================================================*/
        // 初始化course表记录，记录根据RecordGenerator.COURSES元素个数确定
        //     course与teacher是多对一关系，即：一个老师可以教多个课程，但是一个课程只有一个老师
        /*============================================)=========================*/
        final List<String> courses = RecordGenerator.getCourses();
        final int courseSize = courses.size();
        final List<Course> courseRecords = new ArrayList<>(courseSize);
        Result<String> initCourseResult = initCourseTable(teacherRecordCount, courses, courseRecords);
        // 初始化Course表失败，则返回结果
        if (!initCourseResult.result()) {
            return initCourseResult;
        }


        /*=====================================================================*/
        // 初始化Schedule表记录，记录由Weekday * Lesson数量确定
        //     schedule与course是一对一关系，即：一条schedule关联一条course
        /*=====================================================================*/
        // 根据权重来排课表， 一周课时：5 * 7 = 35，总课目：13，每个课目一周至少有2课时
        final int[] weight = new int[] {4, 4, 4, 3, 3, 2, 2, 3, 2, 2, 2, 2, 2};
        isTrueArgument("array weight length equals to courses size", weight.length == courseSize);
        Result<String> initScheduleResult = initScheduleTable(courses, weight, courseRecords);
        // 初始化Schedule表失败，则返回结果
        if (!initScheduleResult.result()) {
            return initScheduleResult;
        }

        return Result.success("Successfully initialized all table records");
    }


    @PostMapping("/clear")
    public Result<String> clear() {
        return dbClear();
    }





    private Result<String> dbClear() {
        try {
            // 清除Schedule表记录
            long count = scheduleService.count();
            if (count > 0) {
                do {
                    List<Schedule> scheduleList = scheduleService.getRange(0, (int) (count & 0x7FFFFFFFL));
                    dbService.batch(Schedule.class, scheduleList, BatchMapper.BatchType.DELETE);
                    count -= 0x7FFFFFFFL;
                } while (count > 0);
            }
            // 清除Course表记录
            count = courseService.count();
            if (count > 0) {
                do {
                    List<Course> courseList = courseService.getRange(0, (int) (count & 0x7FFFFFFFL));
                    dbService.batch(Course.class, courseList, BatchMapper.BatchType.DELETE);
                    count -= 0x7FFFFFFFL;
                } while (count > 0);
            }
            // 清除Teacher表记录
            count = teacherService.count();
            if (count > 0) {
                do {
                    List<Teacher> teacherList = teacherService.getRange(0, (int) (count & 0x7FFFFFFFL));
                    dbService.batch(Teacher.class, teacherList, BatchMapper.BatchType.DELETE);
                    count -= 0x7FFFFFFFL;
                } while (count > 0);
            }
            return Result.success("Successfully clear all table records in database");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("Failed clear all table records in database");
        }
    }


    private Result<String> initTeacherTable(final int teacherRecordCount) {
        List<Teacher> teacherRecords = new ArrayList<>(teacherRecordCount);
        final LocalDate birth = LocalDate.of(1997, 3, 15);
        final Gender[] genders = Gender.values();
        final int genderLength = genders.length;
        for (int i = 0; i < teacherRecordCount; i++) {
            Teacher teacher = new Teacher().withName(RecordGenerator.generatedEngName())
                    .withBirth(birth.plusDays(RANDOM.nextInt(1000)))
                    .withGender(genders[RANDOM.nextInt(99) % genderLength]);
            teacherRecords.add(teacher);
        }
        Result<List<Teacher>> teacherInsertResult = dbService.batch(Teacher.class, teacherRecords, BatchMapper.BatchType.INSERT);
        // 如果Teacher batch insert失败
        if (!teacherInsertResult.result()) {
            // 需要强转，细节请查阅 DbServiceImpl::batch(..) 源码
            @SuppressWarnings("unchecked")
            Map<Teacher, Integer> errorInfoMap = (Map<Teacher, Integer>) ((Object) teacherInsertResult.getData());
            // errorInfoMap中的Integer是批处理后返回的结果，我们根据Integer类型的返回值进行分组，获取错误标记的记录
            Map<Integer, List<Teacher>> groupListMap = errorInfoMap.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
            // 获取插入失败的记录信息
            List<Teacher> failedRecord = groupListMap.get(Statement.EXECUTE_FAILED);
            if (!failedRecord.isEmpty()) {
                LOG.info("`teacher` table batch insert failed record: " + failedRecord.stream().map(Teacher::toString).collect(Collectors.joining(System.lineSeparator())));
            }
            return Result.error(teacherInsertResult.getMessage());
        }
        return Result.success("Successfully generated `teacher` table records");
    }


    private Result<String> initCourseTable(final int teacherRecordCount, final List<String> courses, final List<Course> courseRecords) {
        final int courseSize = courses.size();
        // 将与课目关联且与课目数相等的老师的数量，随机减少[1, 3]；若随机减少为0，则继续循环
        int diff;
        do {
            diff = RANDOM.nextInt(4);
        } while (diff <= 0);
        final int relatedTeacherCount = courseSize - diff;
        final List<Teacher> relatedTeacherList = teacherService.getRange(RANDOM.nextInt(teacherRecordCount) - relatedTeacherCount, relatedTeacherCount);
        isTrueArgument("", !relatedTeacherList.isEmpty());

        for (int i = 0; i < courseSize; i++) {
            Course course;
            if (i < relatedTeacherCount) {
                course = new Course().withName(courses.get(i)).withTeacher(relatedTeacherList.get(i));
            } else {
                course = new Course().withName(courses.get(i)).withTeacher(relatedTeacherList.get(RANDOM.nextInt(relatedTeacherCount)));
            }
            courseRecords.add(course);
        }
        Result<List<Course>> courseInsertResult = dbService.batch(Course.class, courseRecords, BatchMapper.BatchType.INSERT);
        // 如果Course batch insert失败
        if (!courseInsertResult.result()) {
            // 需要这样强转，细节请查阅 DbServiceImpl::batch(..) 源码
            @SuppressWarnings("unchecked")
            Map<Course, Integer> errorInfoMap = (Map<Course, Integer>) ((Object) courseInsertResult.getData());
            Map<Integer, List<Course>> groupListMap = errorInfoMap.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
            // 获取插入失败的记录信息
            List<Course> failedRecord = groupListMap.get(Statement.EXECUTE_FAILED);
            if (!failedRecord.isEmpty()) {
                LOG.info("`course` table batch insert failed record: " + failedRecord.stream().map(Course::toString).collect(Collectors.joining(System.lineSeparator())));
            }
            return Result.error(courseInsertResult.getMessage());
        }
        return Result.success("Successfully generated `course` table records");
    }



    private Result<String> initScheduleTable(final List<String> courses, final int[] weight, final List<Course> courseRecords) {
        final int courseSize = courses.size();
        // 生成权重概率
        List<Double> probabilities = new ArrayList<>(weight.length);
        final int sum = IntStream.of(weight).sum();
        for (int i = 0; i < courseSize; i++) {
            double probability = weight[i] * 1d / sum;
            isTrueArgument("probability", probability < 1d);
            probabilities.add(i, probability);
        }
        final AliasMethod aliasMethod = new AliasMethod(probabilities);

        // schedule表记录数
        final int scheduleSize = 35;
        List<Schedule> scheduleRecords = new ArrayList<>(scheduleSize);
        Lesson[] lessons = Lesson.values();
        Weekday[] weekday = Weekday.values();
        for (Weekday value : weekday) {
            for (Lesson lesson : lessons) {
                Schedule s = new Schedule()
                        .withLesson(lesson)
                        .withWeekday(value)
                        .withCourse(courseRecords.get(aliasMethod.next()));
                scheduleRecords.add(s);
            }
        }
        final Result<List<Schedule>> scheduleInsertResult = dbService.batch(Schedule.class, scheduleRecords, BatchMapper.BatchType.INSERT);
        // 如果Schedule batch insert失败
        if (!scheduleInsertResult.result()) {
            // 需要这样强转，细节请查阅 DbServiceImpl::batch(..) 源码
            @SuppressWarnings("unchecked")
            Map<Schedule, Integer> errorInfoMap = (Map<Schedule, Integer>) ((Object) scheduleInsertResult.getData());
            Map<Integer, List<Schedule>> groupListMap = errorInfoMap.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
            // 获取插入失败的记录信息
            List<Schedule> failedRecord = groupListMap.get(Statement.EXECUTE_FAILED);
            if (!failedRecord.isEmpty()) {
                LOG.info("`schedule` table batch insert failed record: " + failedRecord.stream().map(Schedule::toString).collect(Collectors.joining(System.lineSeparator())));
            }
            return Result.error(scheduleInsertResult.getMessage());
        }
        return Result.success("Successfully generated `schedule` table records");
    }



    static class RecordGenerator {
        private static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz".toCharArray();  // 0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ
        static final Random RANDOM = ThreadLocalRandom.current();
        private static final char BLANK = ' ';
        private static final List<String> COURSES = List.of("Chinese", "Maths", "English", "Physics", "Chemistry",
                "Biology", "Politics", "History", "Geography", "Music", "Art", "Physical Education", "Moral Education");

        static String generatedEngName() {
            final int loops = Math.max(8, RANDOM.nextInt(18));
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < loops; i++) {
                char ch = ALPHABET[RANDOM.nextInt(99) % ALPHABET.length];
                sb.append(0 == i ? Character.toUpperCase(ch) : ch);
            }
            final int idx = Math.floorDiv(sb.length(), 2);
            sb.insert(idx, Character.toUpperCase(sb.charAt(idx)));
            sb.insert(idx, BLANK);
            return sb.toString();
        }


        static List<String> getCourses() {
            return COURSES;
        }

    }


    /**
     * <p>Author: Keith Schwarz (htiek@cs.stanford.edu)
     * <p>https://www.keithschwarz.com/darts-dice-coins/
     * <p>Modified by: Sun Xiaodong, 2022/05/18
     */
    // 别名算法
    static final class AliasMethod {
        private final double epsilon = 1e-15; // 指定一个double可比较的最小精度
        private final Random random;
        private final int[] alias;
        private final double[] prob;

        public AliasMethod(List<Double> probabilities) {
            this(probabilities, ThreadLocalRandom.current());
        }

        public AliasMethod(final List<Double> probabilities, Random random) {
            notNull("probabilities", probabilities);
            notNull("random", random);
            isTrueArgument("probabilities must be nonempty", !probabilities.isEmpty());

            this.prob = new double[probabilities.size()];
            this.alias = new int[probabilities.size()];
            this.random = random;

            init(probabilities);
        }

        private void init(final List<Double> probabilities) {
            final double average = 1d / probabilities.size();
            Deque<Integer> small = new ArrayDeque<>();
            Deque<Integer> large = new ArrayDeque<>();

            for (int i = 0, size = probabilities.size(); i < size; i++) {
                final double diff = probabilities.get(i) - average;
                if (diff > 0 || Math.abs(diff) < epsilon) {  // probabilities.get(i) >= average
                    large.add(i);
                } else {
                    small.add(i);
                }
            }

            while (!small.isEmpty() && !large.isEmpty()) {
                final int less = small.removeFirst();
                final int more = large.removeFirst();

                prob[less] = probabilities.get(less) * probabilities.size();
                alias[less] = more;
                probabilities.set(more, (probabilities.get(more) + probabilities.get(less)) - average);
                final double diff = probabilities.get(more) - average;
                if (diff > 0 || Math.abs(diff) < epsilon) {  // probabilities.get(more) >= average
                    large.add(more);
                } else {
                    small.add(more);
                }
            }

            // 当入参的概率总和小于1（分数转换成小数计算时精度丢失）或大于1（四舍五入）时，直接取为1，概率有稍许变化
            while (!small.isEmpty()) {
                prob[small.removeFirst()] = 1d;
            }
            while (!large.isEmpty()) {
                prob[large.removeFirst()] = 1d;
            }
            //System.err.println("Alias Algorithm porb:" + System.lineSeparator() + DoubleStream.of(prob).mapToObj(Double::toString).collect(Collectors.joining(System.lineSeparator())));
            //System.err.println("Alias Algorithm alias:" + System.lineSeparator() + IntStream.of(alias).mapToObj(Integer::toString).collect(Collectors.joining(System.lineSeparator())));
        }


        public int next() {
            final int column = random.nextInt(prob.length);
            final double diff = random.nextDouble() - prob[column];
            boolean coinToss = diff > 0 || Math.abs(diff) < epsilon;  // random.nextDouble() >= prob[column]
            return coinToss ? alias[column] : column;
        }
    }
}
