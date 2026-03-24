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

    @Test
    @DisplayName("생일_하루_전이면_false_반환")
    void isOverAge_oneDayBeforeBirthday_returnsFalse() {
        LocalDate birthDay = LocalDate.now().minusYears(19).plusDays(1);

        assertThat(AgeUtil.isOverAge(birthDay, 19)).isFalse();
    }

    @Test
    @DisplayName("age가_0이면_항상_true_반환_전체이용가")
    void isOverAge_ageZero_alwaysTrue() {
        LocalDate birthDate = LocalDate.now();

        assertThat(AgeUtil.isOverAge(birthDate, 0)).isTrue();
    }
}