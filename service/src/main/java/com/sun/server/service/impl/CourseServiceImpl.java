package com.sun.server.service.impl;

import com.sun.common.entity.Course;
import com.sun.server.mapper.CourseMapper;
import com.sun.server.service.CourseService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * @description:
 * @author: Sun Xiaodong
 */
@Service
public class CourseServiceImpl implements CourseService {
    @PersistenceContext
    private EntityManager entityManager;

    private CourseMapper courseMapper;

    CourseServiceImpl() {}

    @PostConstruct
    private void init() {
        this.courseMapper = new CourseMapper(this.entityManager);
    }

    private CourseMapper mapper() {
        return this.courseMapper;
    }

    @Override
    public Long count() {
        return mapper().count(Course.class);
    }

    @Override
    public List<Course> getRange(int startPosition, int maxResults) {
        return mapper().getRange(Course.class, startPosition, maxResults);
    }
}
