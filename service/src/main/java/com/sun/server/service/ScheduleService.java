package com.sun.server.service;

import com.sun.common.entity.Schedule;

import java.util.List;

/**
 * @description:
 * @author: Sun Xiaodong
 */
public interface ScheduleService extends Service {
    Long count();
    List<Schedule> getRange(int startPosition, int maxResults);
}
