package com.sun.client.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sun.client.feign.FeignService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: Sun Xiaodong
 */
@RestController
@RequestMapping("/request")
@JsonIgnoreProperties()
public class ClientController implements BaseController {
    @Resource
    private FeignService feignService;


    @GetMapping("/db/init")
    public Result<String> dbInit() {
        return feignService.init();
    }

    @GetMapping("/db/clear")
    public Result<String> dbClear() {
        return feignService.clear();
    }


    // 查询老师信息：无参数，则查询所有老师；有参数，则根据参数设置条件查找老师
    @GetMapping("/query/teacher")
    public Result<List<Teacher>> getTeacherTableRecord(
            @RequestParam(name = "id", required = false) String id,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "gender", required = false) String gender,
            @RequestParam(name = "birth", required = false) String birth) {
        Map<String, String> params = new HashMap<>(4);

        if (!StringUtil.isBlank(id)) {
            params.put("id", StringUtil.strip(id));
        }

        if (!StringUtil.isBlank(name)) {
            params.put("name", StringUtil.strip(name));
        }

        if (!StringUtil.isBlank(gender)) {
            Gender g = findEnum(Gender.values(), gender);
            if (null != g) {
                params.put("gender", g.code().toString());
            } else {
                return Result.error("gender invalid");
            }
        }

        if (!StringUtil.isBlank(birth)) {
            birth = StringUtil.strip(birth);
            try {
                LocalDateUtil.toLocalDate(birth);
                params.put("birth", birth);
            } catch (DateTimeParseException e) {
                // 参数不合法
                return Result.error("The birth date format error, it must be formatted as yyyy-MM-dd");
            }
        }
        return feignService.getTeacherTableRecord(params);
    }


    // 查询课目信息：无参数，则查询所有课目；有参数，则根据参数设置条件查找课目
    @GetMapping("/query/course")
    public Result<List<Course>> getCourseTableRecord(
            @RequestParam(name = "id", required = false) String id,
            @RequestParam(name = "name", required = false) String name) {

        Map<String, String> params = new HashMap<>(4);
        if (!StringUtil.isBlank(id)) {
            params.put("id", StringUtil.strip(id));
        }

        if (!StringUtil.isBlank(name)) {
            params.put("name", StringUtil.strip(name));
        }
        return feignService.getCourseTableRecord(params);
    }


    // 查询课程安排信息：无参数，则查询所有课程安排；有参数，则根据参数设置条件查找课程安排
    @GetMapping("/query/schedule")
    public Result<List<Schedule>> getScheduleTableRecord(
            @RequestParam(name = "id", required = false) String id,
            @RequestParam(name = "lesson", required = false) String lesson,
            @RequestParam(name = "weekday", required = false) String weekday) {

        Map<String, String> params = new HashMap<>(4);
        if (!StringUtil.isBlank(id)) {
            params.put("id", StringUtil.strip(id));
        }

        if (!StringUtil.isBlank(lesson)) {
            Lesson l = findEnum(Lesson.values(), lesson);
            if (null != l) {
                params.put("lesson", l.code().toString());
            } else {
                return Result.error("lesson invalid");
            }
        }

        if (!StringUtil.isBlank(weekday)) {
            Weekday w = findEnum(Weekday.values(), weekday);
            if (null != w) {
                params.put("weekday", w.code().toString());
            } else {
                return Result.error("weekday invalid");
            }
        }

        return feignService.getScheduleTableRecord(params);
    }


    // 多表查询：查询某个老师的所有课程安排
    @GetMapping("/query/multitable/schedule")
    public Result<List<ScheduleofTeacherVo>> getScheduleOfSpecifiedTeacher(@RequestParam(value = "id", required = false) String teacherId) {
        if (!StringUtil.isBlank(teacherId)) {
            return feignService.getScheduleOfSpecifiedTeacher(teacherId);
        } else {
            return Result.error("teacher id required");
        }
    }


    // 多表查询：查询课程安排中的所有老师
    @GetMapping("/query/multitable/teacher")
    public Result<List<Teacher>> getTeacherFromSchedule() {
        return feignService.getTeacherFromSchedule();
    }


    @GetMapping("/query/multitable/maxcountcourse")
    public Result<Map<String, Long>> getMaxCountCourseFromSchedule() {
        return feignService.getMaxCountCourseFromSchedule();
    }

}
