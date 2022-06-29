package com.sun.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @description: LocalDate、LocalDateTime工具类
 * @author: Sun Xiaodong
 */
public final class LocalDateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * <p>java.time.LocalDate --> java.util.Date
     * @param localDate  LocalDate对象
     * @return  返回转换后的Date对象
     */
    public static Date localDateToDate(final LocalDate localDate) {
        final Instant instant = localDate.atStartOfDay(ZoneId.of(Constants.DEFAULT_TIME_ZONE)).toInstant();
        return Date.from(instant);
        // LocalDate对象转java.sql.Date，直接调用方法 java.sql.Date.valueOf(localDate) 即可;
    }

    /**
     * <p>java.util.Date --> java.time.LocalDate
     * @param date  Date对象
     * @return  返回转换后的LocalDate对象
     */
    public static LocalDate dateToLocalDate(final Date date) {
        final Instant instant = Instant.ofEpochMilli(date.getTime());
        return instant.atZone(ZoneId.of(Constants.DEFAULT_TIME_ZONE)).toLocalDate();
        // java.sql.Date对象sqlDate转localDate，直接调用 sqlDate.toLocalDate()方法即可
    }


    /**
     * <p>java.time.LocalDateTime --> java.util.Date
     * @param localDateTime  LocalDateTime对象
     * @return  返回转换后的Date对象
     */
    public static Date localDateTimeToDate(final LocalDateTime localDateTime) {
        final Instant instant = localDateTime.atZone(ZoneId.of(Constants.DEFAULT_TIME_ZONE)).toInstant();
        return Date.from(instant);
    }

    /**
     * <p>java.util.Date --> java.time.LocalDateTime
     * @param date  Date对象
     * @return  返回转换后的LocalDateTime对象
     */
    public static LocalDateTime dateToLocalDateTime(final Date date) {
        final Instant instant = Instant.ofEpochMilli(date.getTime());
        return instant.atZone(ZoneId.of(Constants.DEFAULT_TIME_ZONE)).toLocalDateTime();
    }


    /**
     * <p>java.time.LocalDate格式化为字符串
     * @param localDate  LocalDate对象
     * @return  格式化后的日期字符串
     */
    public static String toString(final LocalDate localDate) {
        return localDate.format(DATE_FORMATTER);
    }

    /**
     * <p>java.time.localDateTime格式化为字符串
     * @param localDateTime  LocalDateTime对象
     * @return  格式化后的日期字符串
     */
    public static String toString(final LocalDateTime localDateTime) {
        return localDateTime.format(DATE_TIME_FORMATTER);
    }


    /**
     * 日期字符串转LocalDate
     * @param localDate  日期字符串
     * @return  LocalDate对象
     */
    public static LocalDate toLocalDate(final String localDate) {
        return LocalDate.parse(localDate, DATE_FORMATTER);
    }

    /**
     * 日期字符串转LocalDateTime
     * @param localDateTime  日期字符串
     * @return  LocalDateTime对象
     */
    public static LocalDateTime toLocalDateTime(final String localDateTime) {
        return LocalDateTime.parse(localDateTime, DATE_TIME_FORMATTER);
    }


    private LocalDateUtil() {
        throw new IllegalStateException("Instantiation not allow");
    }
}
