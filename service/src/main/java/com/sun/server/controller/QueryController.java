package com.sun.server.controller;

import com.sun.common.entity.Course;
import com.sun.common.entity.Schedule;
import com.sun.common.entity.Teacher;
import com.sun.common.enumeration.Gender;
import com.sun.common.enumeration.Lesson;
import com.sun.common.enumeration.Weekday;
import com.sun.common.util.LocalDateUtil;
import com.sun.common.util.Result;
import com.sun.common.util.StringUtil;
import com.sun.common.vo.ScheduleofTeacherVo;
import com.sun.server.service.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sun.common.util.ReflectionUtil.getFieldName;

/**
 * @description: 业务查询
 * @author: Sun Xiaodong
 */
@RestController
@RequestMapping("/query")
public class QueryController implements BaseController {
    private static final Logger LOG = LoggerFactory.getLogger(QueryController.class);
    @Resource
    private QueryService queryService;

    // name, gender, birth
    @PostMapping("/teacher")
    public Result<List<Teacher>> getTeacherTableRecord(@RequestBody Map<String, String> map) {
        try {
            if (map.isEmpty()) {
                final Long count = queryService.count(Teacher.class);
                if (count > 0) {
                    List<Teacher> resultList = queryService.getRange(Teacher.class, 0, (int) (count & 0x7FFFFFFFL));
                    return Result.success(resultList);
                }
                return Result.success(null, "`teacher` table no records");
            } else {
                final String idFieldName = getFieldName(Teacher::getId);
                final String nameFieldName = getFieldName(Teacher::getName);
                final String genderFieldName = getFieldName(Teacher::getGender);
                final String birthFieldName = getFieldName(Teacher::getBirth);

                String id = StringUtil.strip(map.get(idFieldName));
                String name = StringUtil.strip(map.get(nameFieldName));
                String gender = StringUtil.strip(map.get(genderFieldName));
                String birth = StringUtil.strip(map.get(birthFieldName));

                final Map<String, Object> conditions = new HashMap<>(map.size());
                // 根据id查找
                if (!StringUtil.isBlank(id)) {
                    conditions.put(idFieldName, id);
                }
                // 根据姓名查找
                if (!StringUtil.isBlank(name)) {
                    conditions.put(nameFieldName, name);
                }
                // 根据性别查找
                Gender g = findEnum(Gender.values(), gender);
                if (null != g) {
                    conditions.put(genderFieldName, g);
                }
                // 根据生日查找
                if (!StringUtil.isBlank(birth)) {
                    try {
                        LocalDate birthDate = LocalDateUtil.toLocalDate(birth);
                        conditions.put(birthFieldName, birthDate);
                    } catch (DateTimeParseException e) {
                        return Result.error("The birth date format error, it must be formatted as yyyy-MM-dd");
                    }
                }

                List<Teacher> teacherList = queryService.query(Teacher.class, conditions);
                return teacherList.isEmpty() ? Result.success(null, "no matching record") : Result.success(teacherList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(null, e);
        }
    }

    @PostMapping("/course")
    public Result<List<Course>> getCourseTableRecord(@RequestBody Map<String, String> map) {
        try {
            if (map.isEmpty()) {
                final Long count = queryService.count(Course.class);
                if (count > 0) {
                    List<Course> resultList = queryService.getRange(Course.class, 0, (int) (count & 0x7FFFFFFFL));
                    return Result.success(resultList);
                }
                return Result.success(null, "`course` table no records");
            } else {
                final String idFieldName = getFieldName(Course::getId);
                final String nameFieldName = getFieldName(Course::getName);

                String id = StringUtil.strip(map.get(idFieldName));
                String name = StringUtil.strip(map.get(nameFieldName));

                final Map<String, Object> conditions = new HashMap<>(map.size());
                //根据id查找
                if (!StringUtil.isBlank(id)) {
                    conditions.put(idFieldName, id);
                }
                // 根据名字查找
                if (!StringUtil.isBlank(name)) {
                    conditions.put(nameFieldName, name);
                }
                List<Course> courseList = queryService.query(Course.class, conditions);
                return courseList.isEmpty() ? Result.success(null, "no matching record") : Result.success(courseList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(null, e);
        }
    }

    @PostMapping("/schedule")
    public Result<List<Schedule>> getScheduleTableRecord(@RequestBody Map<String, String> map) {
        try {
            if (map.isEmpty()) {
                final Long count = queryService.count(Schedule.class);
                if (count > 0) {
                    List<Schedule> resultList = queryService.getRange(Schedule.class, 0, (int) (count & 0x7FFFFFFFL));
                    return Result.success(resultList);
                }
                return Result.success(null, "`schedule` table no records");
            } else {
                final String idFieldName = getFieldName(Schedule::getId);
                final String lessonFieldName = getFieldName(Schedule::getLesson);
                final String weekdayFieldName = getFieldName(Schedule::getWeekday);

                String id = StringUtil.strip(map.get(idFieldName));
                String lesson = StringUtil.strip(map.get(lessonFieldName));
                String weekday = StringUtil.strip(map.get(weekdayFieldName));

                final Map<String, Object> conditions = new HashMap<>(map.size());
                //根据id查找
                if (!StringUtil.isBlank(id)) {
                    conditions.put(idFieldName, id);
                }

                // 根据lesson查找
                Lesson l = findEnum(Lesson.values(), lesson);
                if (null != l) {
                    conditions.put(lessonFieldName, l);
                }

                // 根据weekday查找
                Weekday w = findEnum(Weekday.values(), weekday);
                if (null != w) {
                    conditions.put(weekdayFieldName, w);
                }

                List<Schedule> scheduleList = queryService.query(Schedule.class, conditions);
                return scheduleList.isEmpty() ? Result.success(null, "no matching record") : Result.success(scheduleList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(null, e);
        }
    }


    // 多表查询：查询某个老师的所有课程安排
    @PostMapping("/multitable/schedule")
    public Result<List<ScheduleofTeacherVo>> getScheduleOfSpecifiedTeacher(@RequestBody String teacherId) {
        try {
            if (!StringUtil.isBlank(teacherId)) {
                teacherId = StringUtil.strip(teacherId);
                final long start = System.nanoTime();
                List<ScheduleofTeacherVo> result = queryService.queryScheduleOfSpecifiedTeacher(teacherId);
                LOG.info("query costs {}ms", (System.nanoTime() - start) / 1000_1000);
                return result.isEmpty() ? Result.success(null, "no matching record") : Result.success(result);
            }
            return Result.error("Invalid teacher id");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(null, e);
        }
    }


    // 多表查询：查询课程安排中的所有老师
    @PostMapping("/multitable/teacher")
    public Result<List<Teacher>> getTeacherFromSchedule() {
        try {
            final long start = System.nanoTime();
            List<Teacher> result = queryService.queryAllTeacherFromSchedule();
            LOG.info("query costs {}ms", (System.nanoTime() - start) / 1000_1000);
            return result.isEmpty() ? Result.success(null, "no matching record") : Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(null, e);
        }
    }



    // 多表查询：统计课程安排表中数量最多的课目；如果有多个课目是最多数量的，那么输出所有最多数量的科目
    @PostMapping("/multitable/maxcountcourse")
    public Result<Map<String, Long>> getMaxCountCourseFromSchedule() {
        try {
            final long start = System.nanoTime();
            Map<String, Long> result = queryService.getMaxCountCourseFromSchedule();
            LOG.info("query costs {}ms", (System.nanoTime() - start) / 1000_1000);
            return result.isEmpty() ? Result.success(null, "no matching record") : Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(null, e);
        }
    }

}
