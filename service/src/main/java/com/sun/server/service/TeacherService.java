package com.sun.server.service;

import com.sun.common.entity.Teacher;

import java.util.List;

/**
 * @description:
 * @author: Sun Xiaodong
 */
public interface TeacherService extends Service {
    Teacher save(Teacher teacher);
    void update(Teacher teacher);
    Long count();
    List<Teacher> getRange(int startPosition, int maxResults);
}
