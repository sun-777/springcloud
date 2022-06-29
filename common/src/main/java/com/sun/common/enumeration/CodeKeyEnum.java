package com.sun.common.enumeration;

import java.util.Arrays;
import java.util.Optional;

/**
 *  See: <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-8.html#jls-8.1.2">https://docs.oracle.com/javase/specs/jls/se8/html/jls-8.html#jls-8.1.2</a>
 *  <p>AdditionalBound:
 *      & InterfaceType
 * 
 *  <p>The enum values are initialized before any other static fields.
 * 
 *  @author Sun xiaodong
 */
public interface CodeKeyEnum<T extends Enum<T> & CodeKeyEnum<T, C, K>, C, K> extends CodeEnum<T, C> {
    K key();

    /**
     * 根据给定的枚举对象的属性值k，查找枚举对象
     * @param k  枚举对象的属性值
     * @return  返回枚举类的Optional对象：如果枚举类T中有k值，则返回k值对应的枚举类T的Optional对象； 没有则返回Optional的空的实例。
     */
    Optional<T> keyOf(K k);

    /**
     * 根据给定的枚举对象的属性值k，查找枚举对象
     * @param enumClass  枚举类型类T
     * @param f  属性值
     * @return  返回枚举类的Optional对象：如果枚举类T中有k值，则返回k值对应的枚举类T的Optional对象； 没有则返回Optional的空的实例。
     * @param <U>  枚举类的泛型声明
     * @param <E>  枚举类属性的泛型声明
     * @param <F>  枚举类属性的泛型声明
     */
    default <U extends Enum<U> & CodeKeyEnum<U, E, F>, E, F> Optional<U> keyOf(Class<U> enumClass, F f) {
        if (f instanceof String) {   //如果是String类型，则不分大小写进行比较
            return Arrays.stream(enumClass.getEnumConstants()).filter(o -> ((String) o.key()).equalsIgnoreCase((String) f)).findAny();
        } else {
            return Arrays.stream(enumClass.getEnumConstants()).filter(o -> o.key().equals(f)).findAny();
        }
    }
}
