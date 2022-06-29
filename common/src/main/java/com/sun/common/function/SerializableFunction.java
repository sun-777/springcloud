package com.sun.common.function;

import java.io.Serializable;

/**
 * @description: 可序列化Function接口类
 * @author: Sun Xiaodong
 */
@FunctionalInterface
public interface SerializableFunction<T> extends Serializable {
    Serializable apply(T t);
}