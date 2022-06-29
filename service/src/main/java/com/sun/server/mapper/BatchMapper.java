package com.sun.server.mapper;

import com.sun.common.entity.Entity;

import java.util.List;

/**
 * @description:  实体映射批处理接口
 * @author: Sun Xiaodong
 */
public interface BatchMapper<T extends Entity> extends Mapper {
    /**
     * 批量保存实体类对象 （通过 Session.doWork 方法，使用JDBC原生API，实现高效的批处理）
     * @param entities 实体类对象List集合
     */
    List<Integer> batchSave(List<T> entities);
    List<Integer> batchUpdate(List<T> entities);
    List<Integer> batchDelete(List<T> entities);

    enum BatchType {
        INSERT,
        UPDATE,
        DELETE;
    }
}
