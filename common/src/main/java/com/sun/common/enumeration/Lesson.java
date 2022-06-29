package com.sun.common.enumeration;

import com.sun.common.util.StringUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description: 课序
 * @author: Sun Xiaodong
 */
public enum Lesson implements CodeKeyEnum<Lesson, Short, String> {
    FIRST((short) 1, "第一节"),
    SECOND((short) 2, "第二节"),
    THIRD((short) 3, "第三节"),
    FOURTH((short) 4, "第四节"),
    FIFTH((short) 5, "第五节"),
    SIXTH((short) 6, "第六节"),
    SEVENTH((short) 7, "第七节");

    private final Short code;
    private final String key;

    private static final Map<Short, Lesson> CODE_MAP;
    private static final Map<String, Lesson> KEY_MAP;

    static {
        CODE_MAP = List.of(Lesson.values()).stream().collect(Collectors.toUnmodifiableMap(Lesson::code, Function.identity()));
        KEY_MAP = List.of(Lesson.values()).stream().collect(Collectors.toUnmodifiableMap(o -> o.key().toLowerCase(Locale.ENGLISH), Function.identity()));
    }
    Lesson(Short code, String key) {
        this.code = code;
        this.key = key;
    }

    @Override
    public Short code() {
        return this.code;
    }

    @Override
    public Optional<Lesson> codeOf(Short c) {
        return Optional.ofNullable(null == c ? null : CODE_MAP.get(c));
    }

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public Optional<Lesson> keyOf(String k) {
        return Optional.ofNullable(StringUtil.isBlank(k) ? null : KEY_MAP.get(k.toLowerCase(Locale.ENGLISH)));
    }
}
