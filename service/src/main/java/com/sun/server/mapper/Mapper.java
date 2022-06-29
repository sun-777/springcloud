package com.sun.server.mapper;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.persister.entity.EntityPersister;

import javax.persistence.EntityManager;
import java.io.Serializable;

/**
 * @description: 映射接口类
 * @author: Sun Xiaodong
 */
public interface Mapper {

    // 获得EntityManager对象
    EntityManager getEntityManager();


    // 批处理时的批大小
    default int getBatchSize() {
        final Object object = getEntityManager().getEntityManagerFactory().getProperties().get(AvailableSettings.STATEMENT_BATCH_SIZE);
        // batch size range [32, 1024]
        return Math.min(1024, Math.max(32, (null == object ? 0 : Integer.parseInt(object.toString()))));
    }


    /**
     *
     * 获取EntityPersister对象
     * @param clazz  实体类的类文字
     * @param <T>  泛型声明
     * @return  EntityPersister对象
     */
    default <T extends Serializable> EntityPersister getEntityPersister(Class<T> clazz) {
        return ((MetamodelImplementor) getEntityManager().getEntityManagerFactory().getMetamodel()).entityPersister(clazz);
    }


}
