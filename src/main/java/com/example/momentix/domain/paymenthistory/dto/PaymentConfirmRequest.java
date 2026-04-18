package com.example.momentix.domain.paymenthistory.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentConfirmRequest {
    private final Long reservationId;
    private long pointsToUse;

    public PaymentConfirmRequest(
            @JsonProperty("reservationId") Long reservationId,
            @JsonProperty("pointsToUse") long pointsToUse
    ) {
        this.reservationId = reservationId;
        this.pointsToUse = pointsToUse;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public long getPointsToUse() {
        return pointsToUse;
    }
}
