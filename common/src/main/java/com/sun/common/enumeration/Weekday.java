package com.sun.common.enumeration;


import com.sun.common.util.StringUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description: 工作日
 * @author: Sun Xiaodong
 */
public enum Weekday implements CodeKeyEnum<Weekday, Short, String> {
    MONDAY((short) 1, "周一"),
    TUESDAY((short) 2, "周二"),
    WEDNESDAY((short) 3, "周三"),
    THURSDAY((short) 4, "周四"),
    FRIDAY((short) 5, "周五");
    //SATURDAY((short) 6, "周六"),
    //SUNDAY((short) 7, "周日");

    private final Short code;
    private final String key;

    private static final Map<Short, Weekday> CODE_MAP;
    private static final Map<String, Weekday> KEY_MAP;

    static {
        CODE_MAP = List.of(Weekday.values()).stream().collect(Collectors.toUnmodifiableMap(Weekday::code, Function.identity()));
        KEY_MAP = List.of(Weekday.values()).stream().collect(Collectors.toUnmodifiableMap(o -> o.key().toLowerCase(Locale.ENGLISH), Function.identity()));
    }


    Weekday(Short code, String key) {
        this.code = code;
        this.key = key;
    }

    @Override
    public Short code() {
        return this.code;
    }

    @Override
    public Optional<Weekday> codeOf(Short c) {
        return Optional.ofNullable(null == c ? null : CODE_MAP.get(c));
    }

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public Optional<Weekday> keyOf(String k) {
        return Optional.ofNullable(StringUtil.isBlank(k) ? null : KEY_MAP.get(k.toLowerCase(Locale.ENGLISH)));
    }
}
