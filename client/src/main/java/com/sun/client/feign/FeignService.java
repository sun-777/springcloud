package com.sun.client.feign;

import com.sun.common.entity.Course;
import com.sun.common.entity.Schedule;
import com.sun.common.entity.Teacher;
import com.sun.common.util.Result;
import com.sun.common.vo.ScheduleofTeacherVo;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: Sun Xiaodong
 */
@Component
@FeignClient(value = "${service-url.nacos-service}")
public interface FeignService {

    // 数据库所有的表记录初始化（会先执行清除clear()）
    @RequestLine("POST /db/init")
    Result<String> init();

    // 清空数据库所有的表记录（表与表之间的数据可能存在外键约束，故不支持单表记录清除）
    @RequestLine("POST /db/clear")
    Result<String> clear();

    // 单表查询：查询老师相关记录
    @RequestLine("POST /query/teacher")
    Result<List<Teacher>> getTeacherTableRecord(@RequestBody Map<String, String> map);

    // 单表查询：查询课目相关记录
    @RequestLine("POST /query/course")
    Result<List<Course>> getCourseTableRecord(@RequestBody Map<String, String> map);

    // 单表查询：查询课程安排相关记录
    @RequestLine("POST /query/schedule")
    Result<List<Schedule>> getScheduleTableRecord(@RequestBody Map<String, String> map);


    // 多表查询：查询某位老师一周的所有课程安排
    @RequestLine("POST /query/multitable/schedule")
    Result<List<ScheduleofTeacherVo>> getScheduleOfSpecifiedTeacher(@RequestBody String teacherId);

    // 多表查询：查询课程安排中的所有老师
    @RequestLine("POST /query/multitable/teacher")
    Result<List<Teacher>> getTeacherFromSchedule();

    // 多表查询：查询课程安排中的数量最多的课目（有多个数量最多的课目，一起查询出来）
    @RequestLine("POST /query/multitable/maxcountcourse")
    Result<Map<String, Long>> getMaxCountCourseFromSchedule();

}
