package com.example.momentix.domain.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AgeUtilTest {

    @Test
    @DisplayName("생일_null이면_false_반환")
    void isOverAge_nullBirthDate_returnsFalse() {
        assertThat(AgeUtil.isOverAge(null, 19)).isFalse();
    }
}