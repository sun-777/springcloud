package com.sun.server.service.impl;

import com.sun.common.entity.Teacher;
import com.sun.server.mapper.TeacherMapper;
import com.sun.server.service.TeacherService;
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
public class TeacherServiceImpl implements TeacherService {

    @PersistenceContext
    private EntityManager entityManager;

    private TeacherMapper teacherMapper;

    TeacherServiceImpl() {}


    @PostConstruct
    private void init() {
        this.teacherMapper = new TeacherMapper(this.entityManager);
    }


    private TeacherMapper mapper() {
        return this.teacherMapper;
    }


    @Override
    public Teacher save(Teacher teacher) {
        return mapper().save(teacher);
    }


    @Override
    public void update(Teacher teacher) {
        mapper().update(teacher);
    }

    @Override
    public Long count() {
        return mapper().count(Teacher.class);
    }

    @Override
    public List<Teacher> getRange(int startPosition, int maxResults) {
        return mapper().getRange(Teacher.class, startPosition, maxResults);
    }
}
