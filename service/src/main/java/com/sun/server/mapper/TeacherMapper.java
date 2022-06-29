package com.sun.server.mapper;

import com.sun.common.entity.Teacher;

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
public class TeacherMapper extends AbstractEntityBatchMapper<Teacher> {

    private final EntityManager em;

    public TeacherMapper(final EntityManager entityManager) {
        this.em = entityManager;
    }

    
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
    
    
    // 获取预处理sql
    @Override
    String getPreparedSql(final BatchType type) {
        StringBuilder sb = new StringBuilder(512);
        switch (type) {
            case INSERT:
                sb.append("insert into `teacher` (id, name, gender, birth) values(?, ?, ?, ?)");
                break;
            case UPDATE:
                sb.append("update `teacher` set name=?, gender=?, birth=? where id=?");
                break;
            case DELETE:
                sb.append("delete from `teacher` where id = ?");
                break;
            default:
                throw new IllegalArgumentException("Unsupported batch operation");
        }
        return sb.toString();
    }

    
    
    // 通过PrepareStatement，设置需要插入、更新、删除的数据
    @Override
    void setParameters(final PreparedStatement ps, final Teacher t, final BatchType type) throws SQLException {
        switch (type) {
            case INSERT:
                // insert into `teacher` (id, name, gender, birth) values(?, ?, ?, ?)
                ps.setString(1, t.getId());
                ps.setString(2, t.getName());
                ps.setShort(3, t.getGender().code());
                ps.setDate(4, java.sql.Date.valueOf(t.getBirth()));
                break;
            case UPDATE:
                // update `teacher` set name=?, gender=?, birth=? where id=?
                ps.setString(1, t.getName());
                ps.setShort(2, t.getGender().code());
                ps.setDate(3, java.sql.Date.valueOf(t.getBirth()));
                ps.setString(4, t.getId());
                break;
            case DELETE:
                // delete from `teacher` where id = ?
                ps.setString(1, t.getId());
                break;
            default:
                throw new IllegalArgumentException("Unsupported batch operation");
        }
    }

}
