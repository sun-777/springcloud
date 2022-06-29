package com.sun.server.service.impl;

import com.sun.common.entity.Schedule;
import com.sun.server.mapper.ScheduleMapper;
import com.sun.server.service.ScheduleService;
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
public class ScheduleServiceImpl implements ScheduleService {

    @PersistenceContext
    private EntityManager entityManager;

    private ScheduleMapper scheduleMapper;

    ScheduleServiceImpl() {}

    @PostConstruct
    private void init() {
        this.scheduleMapper = new ScheduleMapper(this.entityManager);
    }

    private ScheduleMapper mapper() {
        return this.scheduleMapper;
    }

    @Override
    public Long count() {
        return mapper().count(Schedule.class);
    }

    @Override
    public List<Schedule> getRange(int startPosition, int maxResults) {
        return mapper().getRange(Schedule.class, startPosition, maxResults);
    }
}
