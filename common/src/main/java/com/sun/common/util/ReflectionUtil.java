package com.sun.common.util;

import com.sun.common.function.SerializableFunction;

import java.beans.Introspector;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

/**
 * @description:
 * @author: Sun Xiaodong
 */
public final class ReflectionUtil {

    private static final String WRITE_REPLACE_METHOD_NAME = "writeReplace";
    private static final String GET_PREFIX = "get";
    private static final String IS_PREFIX = "is";
    private static final String LAMBDA_PREFIX = "lambda$";


    // 获取 SerializedLambda 对象
    private static SerializedLambda getSerializedLambda(Serializable serializable) throws ReflectiveOperationException, RuntimeException {
        final Method method = serializable.getClass().getDeclaredMethod(WRITE_REPLACE_METHOD_NAME);
        method.setAccessible(true);
        return (SerializedLambda) method.invoke(serializable);
    }


    /**
     * 通过Javabean对象的get或is方法的方法引用返回对应的Field
     * @param serializableFunction  可序列化Function的方法引用
     * @param <T>  泛型声明
     * @return  方法对应的field名
     */
    public static <T> String getFieldName(SerializableFunction<T> serializableFunction) {
        try {
            SerializedLambda serializedLambda = getSerializedLambda(serializableFunction);
            String implMethodName = serializedLambda.getImplMethodName();
            if (implMethodName.startsWith(GET_PREFIX) ) {
                return Introspector.decapitalize(implMethodName.replace(GET_PREFIX, ""));
            } else if (implMethodName.startsWith(IS_PREFIX)) {
                return Introspector.decapitalize(implMethodName.replace(IS_PREFIX, ""));
            } else if (implMethodName.startsWith(LAMBDA_PREFIX)) {
                throw new IllegalArgumentException("SerializableFunction can't passing Lambda expressions，can only use method reference.");
            } else {
                throw new IllegalArgumentException(implMethodName + " is not a JavaBean getter method reference.");
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e.getCause());
        }
    }



    private ReflectionUtil() {
        throw new IllegalStateException("Instantiation not allowed");
    }
}
