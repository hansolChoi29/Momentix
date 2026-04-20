package com.example.momentix.domain.common.util;

import java.time.LocalDate;
import java.time.ZoneId;

public class AgeUtil {
    private static final ZoneId zoneId = ZoneId.of("Asia/Seoul");
    public static boolean isOverAge(LocalDate birthDate, int age) {
        if (birthDate == null) return false;
        LocalDate today = LocalDate.now(zoneId);
        return !today.isBefore(birthDate.plusYears(age));
    }
}
