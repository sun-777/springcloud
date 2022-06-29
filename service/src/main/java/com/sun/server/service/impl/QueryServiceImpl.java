package com.sun.server.service.impl;

import com.sun.common.entity.Entity;
import com.sun.common.entity.Teacher;
import com.sun.common.vo.ScheduleofTeacherVo;
import com.sun.server.mapper.CommonMapper;
import com.sun.server.service.QueryService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: Sun Xiaodong
 */
@Service
public class QueryServiceImpl implements QueryService {
    @PersistenceContext
    private EntityManager entityManager;

    private CommonMapper mapper;

    @PostConstruct
    private void init() {
        this.mapper = new CommonMapper(this.entityManager);
    }


    @Override
    public <T extends Entity> Long count(Class<T> clazz) {
        return mapper.count(clazz);
    }


    @Override
    public <T extends Entity> List<T> getRange(Class<T> clazz, final int startPosition, final int maxResults) {
        return mapper.getRange(clazz, startPosition, maxResults);
    }


    @Override
    public <T extends Entity> List<T> query(Class<T> clazz, final Map<String, Object> conditions) {
        return (List<T>) mapper.query(clazz, conditions);
    }

    public List<ScheduleofTeacherVo> queryScheduleOfSpecifiedTeacher(final String teacherId) {
        return mapper.queryScheduleOfSpecifiedTeacher(teacherId);
    }

    @Override
    public List<Teacher> queryAllTeacherFromSchedule() {
        return mapper.queryAllTeacherFromSchedule();
    }

    public Map<String, Long> getMaxCountCourseFromSchedule() {
        return mapper.getMaxCountCourseFromSchedule();
    }
}
