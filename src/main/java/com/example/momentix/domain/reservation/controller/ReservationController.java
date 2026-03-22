package com.example.momentix.domain.reservation.controller;


import com.example.momentix.domain.reservation.dto.ReservationResponseDto;
import com.example.momentix.domain.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Reservation", description = "예매 관련 API")
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "공연/장소/시간 한 번에 선택", description = "예매 초기 단계 - 공연, 장소, 시간을 한 요청으로 처리")
    @PostMapping("/events/{eventId}/{eventPlaceId}/{eventTimeId}")
    public ResponseEntity<ReservationResponseDto> selectAllReservations(
            @AuthenticationPrincipal String email,
            @PathVariable Long eventId,
            @PathVariable Long eventPlaceId,
            @PathVariable Long eventTimeId
    ) {
        ReservationResponseDto reservations = reservationService.selectAll(
                email,
                eventId,
                eventPlaceId,
                eventTimeId
        );

        return new ResponseEntity<>(
                reservations,
                HttpStatus.OK
        );
    }

    @Operation(summary = "공연 선택", description = "예매 시작 - 공연 선택 단계")
    @PostMapping("/events/{eventId}")
    public ResponseEntity<ReservationResponseDto> selectEvent(
            @AuthenticationPrincipal String email,
            @PathVariable Long eventId
    ) {

        ReservationResponseDto reservations = reservationService.selectEvent(
                email,
                eventId
        );

        return new ResponseEntity<>(reservations, HttpStatus.CREATED);
    }

    @Operation(summary = "공연 장소 선택")
    @PostMapping("/{reservationId}/select-event-place/{eventPlaceId}")
    public ResponseEntity<ReservationResponseDto> selectEventPlace(
            @AuthenticationPrincipal String email,
            @PathVariable Long reservationId,
            @PathVariable Long eventPlaceId
    ) {
        ReservationResponseDto reservationResponseDto
                = reservationService.selectEventPlace(
                email,
                reservationId,
                eventPlaceId
        );

        return new ResponseEntity<>(
                reservationResponseDto,
                HttpStatus.OK
        );
    }


    @Operation(summary = "공연 시간 선택")
    @PostMapping("/{reservationId}/select-event-time/{eventTimeId}")
    public ResponseEntity<ReservationResponseDto> selectEventTime(
            @AuthenticationPrincipal String email,
            @PathVariable Long reservationId,
            @PathVariable Long eventTimeId
    ) {

        ReservationResponseDto reservationResponseDto = reservationService.selectEventTime(
                email,
                reservationId,
                eventTimeId
        );

        return new ResponseEntity<>(
                reservationResponseDto,
                HttpStatus.OK
        );
    }

    @Operation(summary = "예매 취소")
    @DeleteMapping("/cancel/{reservationId}")
    public ResponseEntity<Void> deleteReservation(
            @AuthenticationPrincipal String email,
            @PathVariable Long reservationId
    ) {
        reservationService.deleteReservation(
                email,
                reservationId
        );

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @Operation(summary = "좌석 선택", description = "Redis 분산 락 + DB 낙관적 락으로 동시성 제어")
    @PostMapping("/{reservationId}/seat/{eventTimeReserveSeatId}")
    public ResponseEntity<ReservationResponseDto> selectSeat(
            @AuthenticationPrincipal String email,
            @PathVariable("reservationId") Long reservationId,
            @PathVariable("eventTimeReserveSeatId") Long eventTimeReserveSeatId
    ) {
        ReservationResponseDto dto = reservationService.selectEventSeat(
                email,
                reservationId,
                eventTimeReserveSeatId
        );
        return ResponseEntity.ok(dto);
    }
}
