package com.sun.server.mapper;

import com.sun.common.entity.Course;

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
public class CourseMapper extends AbstractEntityBatchMapper<Course> {

    private final EntityManager em;

    public CourseMapper(final EntityManager entityManager) {
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
                sb.append("insert into `course` (id, name, tid) values(?, ?, ?)");
                break;
            case UPDATE:
                sb.append("update `course` set name=? where id=?");
                break;
            case DELETE:
                sb.append("delete from `course` where id = ?");
                break;
            default:
                throw new IllegalArgumentException("Unsupported batch operation");
        }
        return sb.toString();
    }

    @Override
    void setParameters(PreparedStatement ps, Course t, BatchType type) throws SQLException {
        switch (type) {
            case INSERT:
                // insert into `course` (id, name, tid) values(?, ?, ?)
                ps.setString(1, t.getId());
                ps.setString(2, t.getName());
                ps.setString(3, t.getTeacher().getId());
                break;
            case UPDATE:
                // update `course` set name=?, gender=?, birth=? where id=?
                ps.setString(1, t.getName());
                ps.setString(2, t.getId());
                break;
            case DELETE:
                // delete from `course` where id = ?
                ps.setString(1, t.getId());
                break;
            default:
                throw new IllegalArgumentException("Unsupported batch operation");
        }
    }

}
