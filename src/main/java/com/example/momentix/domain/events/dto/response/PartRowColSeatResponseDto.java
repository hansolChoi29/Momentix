package com.example.momentix.domain.events.dto.response;

import com.example.momentix.domain.events.entity.enums.SeatGradeType;
import com.example.momentix.domain.events.entity.enums.SeatPartType;
import com.example.momentix.domain.events.entity.enums.SeatStatusType;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PartRowColSeatResponseDto {
    private Long id;
    private SeatGradeType seatGradeType;
    private SeatPartType seatPartType;
    private BigDecimal seatPrice;
    // true가 예매 가능한 상태
    private SeatStatusType seatReserveStatus;
    private Long seatRow;
    private Long seatCol;

    public PartRowColSeatResponseDto(Long id, SeatGradeType seatGradeType, SeatPartType seatPartType, BigDecimal seatPrice, SeatStatusType seatReserveStatus, Long seatRow, Long seatCol) {
        this.id = id;
        this.seatGradeType = seatGradeType;
        this.seatPartType = seatPartType;
        this.seatPrice = seatPrice;
        this.seatReserveStatus = seatReserveStatus;
        this.seatRow = seatRow;
        this.seatCol = seatCol;
    }
}
