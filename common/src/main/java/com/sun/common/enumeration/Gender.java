package com.sun.common.enumeration;

import com.sun.common.util.StringUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description: 性别枚举类：持久到数据库中的字段是code属性，所以一旦定义好code值，请不要再修改
 * @author: Sun Xiaodong
 */
public enum Gender implements CodeKeyEnum<Gender, Short, String> {
    MALE((short) 1, "男"),
    FEMALE((short) 2, "女");

    private final Short code;
    private final String key;

    private static final Map<Short, Gender> CODE_MAP;
    private static final Map<String, Gender> KEY_MAP;

    static {
        CODE_MAP = List.of(Gender.values()).stream().collect(Collectors.toUnmodifiableMap(Gender::code, Function.identity()));
        KEY_MAP = List.of(Gender.values()).stream().collect(Collectors.toUnmodifiableMap(o -> o.key().toLowerCase(Locale.ENGLISH), Function.identity()));
    }

    Gender(Short code, String key) {
        this.code = code;
        this.key = key;
    }

    @Override
    public Short code() {
        return this.code;
    }

    @Override
    public Optional<Gender> codeOf(Short c) {
        return Optional.ofNullable(null == c ? null : CODE_MAP.get(c));
    }

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public Optional<Gender> keyOf(String k) {
        return Optional.ofNullable(StringUtil.isBlank(k) ? null : KEY_MAP.get(k.toLowerCase(Locale.ENGLISH)));
    }
}
