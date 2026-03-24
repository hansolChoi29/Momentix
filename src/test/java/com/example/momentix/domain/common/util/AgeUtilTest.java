package com.example.momentix.domain.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AgeUtilTest {

    @Test
    @DisplayName("생일_null이면_false_반환")
    void isOverAge_nullBirthDate_returnsFalse() {
        assertThat(AgeUtil.isOverAge(null, 19)).isFalse();
    }

    @Test
    @DisplayName("만_나이_기준_생일_당일이면_true_반환")
    void isOverAge_exactBirthday_returnsTrue() {
        LocalDate birthDate = LocalDate.now().minusYears(19);
        assertThat(AgeUtil.isOverAge(birthDate, 19)).isTrue();
    }
}