package com.hn.onelabel.server.common.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

public class LocalDateTimeUtils {

    public static Date localDateTime2Date(LocalDateTime localDateTime) {
        if (Objects.isNull(localDateTime)) {
            return null;
        }
        ZoneId zoneId = ZoneId.of("GMT+08:00");
        ZonedDateTime zdt = localDateTime.atZone(zoneId);
        return Date.from(zdt.toInstant());
    }

    public static LocalDateTime date2LocalDateTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.of("GMT+08:00");
        return instant.atZone(zoneId).toLocalDateTime();
    }

    public static Long localDateTime2Long(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static Long localDateTime2Long2Second(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    public static Long localDateTime2Long2MilliSecond(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.ofTotalSeconds(8*60*60)).toEpochMilli();
    }

    public static long getDurationToMinutesBetweenCurAndTargetTime(LocalDateTime endLocalDateTime) {
        if (endLocalDateTime.isBefore(LocalDateTime.now())) {
            return 0L;
        }
        return Duration.between(LocalDateTime.now(), endLocalDateTime).toMinutes();
    }

    public static String getFormatDateString(LocalDateTime localDateTime, String pattern) {
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static Integer getFormatDateInt(LocalDateTime localDateTime, String pattern) {
        return Integer.parseInt(getFormatDateString(localDateTime, pattern));
    }

    public static void main(String[] args) {
        System.out.println(localDateTime2Long(LocalDateTime.now()));
        System.out.println(localDateTime2Long2Second(LocalDateTime.now()));
        System.out.println(localDateTime2Long2MilliSecond(LocalDateTime.now()));
        System.out.println(System.currentTimeMillis());
        System.out.println(LocalDateTime.of(LocalDateTime.now().toLocalDate(), LocalTime.MIN));
        System.out.println(LocalDateTime.of(LocalDateTime.now().plusDays(1).toLocalDate(), LocalTime.MIN));
        System.out.println(LocalDateTime.of(LocalDateTime.now().plusDays(-1).toLocalDate(), LocalTime.MIN));
        System.out.println(LocalDateTime.now().getHour());
        System.out.println(LocalDateTime.of(LocalDateTime.now().plusDays(1).toLocalDate(), LocalTime.MIN));
        System.out.println(getDurationToMinutesBetweenCurAndTargetTime(LocalDateTime.of(LocalDateTime.now().plusDays(-1).toLocalDate(), LocalTime.MIN)));
        System.out.println(getFormatDateString(LocalDateTime.now(), "yyyyMM"));
        System.out.println(getFormatDateInt(LocalDateTime.now(), "yyyyMM"));
    }

}
