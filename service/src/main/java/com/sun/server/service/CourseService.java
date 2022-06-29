package com.sun.server.service;

import com.sun.common.entity.Course;

import java.util.List;

/**
 * @description:
 * @author: Sun Xiaodong
 */
public interface CourseService extends Service {
    Long count();
    List<Course> getRange(int startPosition, int maxResults);
}
