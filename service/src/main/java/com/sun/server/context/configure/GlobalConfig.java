package com.sun.server.context.configure;

import com.sun.common.entity.Entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: 保存全局的配置类
 * @author: Sun Xiaodong
 */
public final class GlobalConfig {

    private GlobalConfig() {
        throw new IllegalStateException("Instantiation not allowed");
    }

    public static Map<Class<? extends Entity>, Class<?>>  getStaticMetamodelMapper() {
        return StaticMetamodelHolder.getHolder();
    }


    /**
     * 存放注解@StaticMetamodel标记的实体类类文字（class literal）与它对应的元模型类类文字（class literal）映射。
     * @author Sun Xiaodong
     */
    private static final class StaticMetamodelHolder {

        private static final Map<Class<? extends Entity>, Class<?>> MAPPER = new ConcurrentHashMap<>(64);

        static Map<Class<? extends Entity>, Class<?>> getHolder() {
            return MAPPER;
        }
    }

}
