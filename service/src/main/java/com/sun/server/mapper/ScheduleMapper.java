package com.sun.server.mapper;

import com.sun.common.entity.Schedule;

import javax.persistence.EntityManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @description: 实体映射类
 *      用法示例：TeacherMapper是AbstractEntityBatchMapper和AbstractEntityMapper的子类
 *      final Mapper mapper = new TeacherMapper(entityManager);
 *       // 批处理
 *       ((BatchMapper) mapper).batchSave(entityList);
 *       // 常规处理
 *       ((EntityMapper) mapper).save(entity);
 *  @author: Sun Xiaodong
 */
public class ScheduleMapper extends AbstractEntityBatchMapper<Schedule> {

    private final EntityManager em;

    public ScheduleMapper(final EntityManager entityManager) {
        this.em = entityManager;
    }


    @Override
    public EntityManager getEntityManager() {
        return this.em;
    }

    
    @Override
    String getPreparedSql(BatchType type) {
        StringBuilder sb = new StringBuilder(512);
        switch (type) {
            case INSERT:
                sb.append("insert into `schedule` (id, weekday, lesson, cid) values(?, ?, ?, ?)");
                break;
            case UPDATE:
                sb.append("update `schedule` set weekday=?, lesson=? where id=?");
                break;
            case DELETE:
                sb.append("delete from `schedule` where id=?");
                break;
            default:
                throw new IllegalArgumentException("Unsupported batch operation");
        }
        return sb.toString();
    }

    @Override
    void setParameters(PreparedStatement ps, Schedule t, BatchType type) throws SQLException {
        switch (type) {
            case INSERT:
                // insert into `schedule` (id, weekday, lesson, cid) values(?, ?, ?, ?)
                ps.setString(1, t.getId());
                ps.setShort(2, t.getWeekday().code());
                ps.setShort(3, t.getLesson().code());
                ps.setString(4, t.getCourse().getId());
                break;
            case UPDATE:
                // update `schedule` set weekday=?, lesson=? where id=?
                ps.setShort(1, t.getWeekday().code());
                ps.setShort(2, t.getLesson().code());
                ps.setString(3, t.getId());
                break;
            case DELETE:
                // delete from `schedule` where id = ?
                ps.setString(1, t.getId());
                break;
            default:
                throw new IllegalArgumentException("Unsupported batch operation");
        }
        
    }

}
