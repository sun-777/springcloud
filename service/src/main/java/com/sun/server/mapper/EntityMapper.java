package com.sun.server.mapper;


import com.sun.common.entity.Entity;

import java.util.List;

/**
 * @description: 实体映射CRUD接口类
 * @author: Sun Xiaodong
 */
public interface EntityMapper<T extends Entity> extends Mapper {

    /**
     * 根据主键，加载实体类对象
     * @param clazz  实体类Class
     * @param primaryKey  主键
     * @return  游离态实体类对象
     */
    T load(Class<T> clazz, Object primaryKey);

    /**
     * 根据主键，加载实体类对象
     * @param clazz  实体类Class
     * @param primaryKey  主键
     * @return  实体类对象
     */
    T get(Class<T> clazz, Object primaryKey);

    /**
     * 将一个游离对象或临时对象的属性复制到一个持久化对象中
     * @param entity  游离对象或临时对象
     * @return  持久化对象
     */
    T save(T entity);
    
    /**
     * 保存对象
     * <p>当不需要使用持久化对象的主键id时，推荐使用persist方法
     * 不保证对象立即被持久化，有可能会推迟到flush的时候才被持久化。
     * 
     * @param entity 实体类对象
     */
    void persist(T entity);
    
    /**
     * 更新实体类对象
     * @param entity  实体类对象
     */
    void update(T entity);
    
    
    /**
     * 保存或更新实体类对象
     * @param entity 游离态或临时的实体类对象
     */
    T saveOrUpdate(T entity);
    
    /**
     * 删除持久化的实体类对象
     * <p>不能删除游离态对象
     * @param entity  持久化对象
     */
    void delete(T entity);

    /**
     * 从指定索引处，获取指定数目的记录（记录为默认的主键索引排序）
     * @param clazz 实体类class
     * @param startPosition 索引开始位置
     * @param maxResults 指定获取的记录数量
     * @return
     */
    List<T> getRange(Class<T> clazz, int startPosition, int maxResults);

    Long count(Class<T> clazz);

}
