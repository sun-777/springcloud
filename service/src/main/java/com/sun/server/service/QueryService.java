package com.sun.server.service;

import com.sun.common.entity.Entity;
import com.sun.common.entity.Teacher;
import com.sun.common.vo.ScheduleofTeacherVo;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: Sun Xiaodong
 */
public interface QueryService extends Service {
    <T extends Entity> Long count(Class<T> clazz);
    <T extends Entity> List<T> getRange(Class<T> clazz, int startPosition, int maxResults);

    // 单表条件查询
    <T extends Entity> List<T> query(Class<T> clazz, final Map<String, Object> conditions);

    // 多表，查询某个老师所有的课程安排
    List<ScheduleofTeacherVo> queryScheduleOfSpecifiedTeacher(String teacherId);

    List<Teacher> queryAllTeacherFromSchedule();

    Map<String, Long> getMaxCountCourseFromSchedule();

}
