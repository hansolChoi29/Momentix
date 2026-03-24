package com.example.momentix.domain.point.service;

import com.example.momentix.domain.common.exception.point.PointErrorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class PointsPolicyServiceTest {
    private final PointsPolicyService pointsPolicyService = new PointsPolicyService();

    @Test
    @DisplayName("10,000원의 3%는 300포인트")
    void calculateEarnPoints_10000won_returns300() {
        long result = pointsPolicyService.calculateEarnPoints(new BigDecimal("10000"));

        assertThat(result).isEqualTo(300L);
    }

    @Test
    @DisplayName("33,333원의 3%는 소수점 내림해서 999포인트")
    void calculateEarnPoints_33333won_returns999() {
        long result = pointsPolicyService.calculateEarnPoints(new BigDecimal("33333"));

        assertThat(result).isEqualTo(999L);
    }

    @Test
    @DisplayName("0원이면 0포인트")
    void calculateEarnPoints_zero_returns0() {
        long result = pointsPolicyService.calculateEarnPoints(BigDecimal.ZERO);

        assertThat(result).isEqualTo(0L);
    }

    @Test
    @DisplayName("null이면 예외 발생")
    void calculateEarnPoints_null_throwsException() {
        assertThatThrownBy(() -> pointsPolicyService.calculateEarnPoints(null))
                .isInstanceOf(PointErrorException.class);
    }

    @Test
    @DisplayName("음수이면 예외 발생")
    void calculateEarnPoints_negative_throwsException() {
        assertThatThrownBy(() -> pointsPolicyService.calculateEarnPoints(new BigDecimal("-1000")))
                .isInstanceOf(PointErrorException.class);
    }
}