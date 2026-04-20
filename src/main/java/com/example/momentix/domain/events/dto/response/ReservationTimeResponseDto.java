package com.example.momentix.domain.events.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationTimeResponseDto {
    private LocalDateTime reservationStartDate;
    private LocalDateTime reservationEndDate;
    
    public ReservationTimeResponseDto(LocalDateTime reservationStartDate, LocalDateTime reservationEndDate) {
        this.reservationStartDate = reservationStartDate;
        this.reservationEndDate = reservationEndDate;
    }
}
