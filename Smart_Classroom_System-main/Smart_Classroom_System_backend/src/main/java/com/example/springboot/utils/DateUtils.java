package com.example.springboot.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class DateUtils {
    // 支持的日期格式列表（扩展常见格式）
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyy年MM月dd日"),
            DateTimeFormatter.ISO_LOCAL_DATE
    );

    // 支持的时间格式列表
    private static final List<DateTimeFormatter> TIME_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("HH:mm:ss")
    );

    /**
     * 解析日期字符串为LocalDate（兼容多格式）
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateStr.trim(), formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new DateTimeParseException("不支持的日期格式: " + dateStr, dateStr, 0);
    }

    /**
     * 解析时间字符串为LocalTime（兼容多格式）
     */
    public static LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                return LocalTime.parse(timeStr.trim(), formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new DateTimeParseException("不支持的时间格式: " + timeStr, timeStr, 0);
    }

    /**
     * 判断目标日期时间是否在当前时间之后
     */
    public static boolean isAfterNow(LocalDate date, LocalTime time) {
        LocalDateTime target = LocalDateTime.of(date, time);
        return target.isAfter(LocalDateTime.now());
    }
}