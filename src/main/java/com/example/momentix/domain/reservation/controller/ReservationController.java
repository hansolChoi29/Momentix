package com.example.momentix.domain.reservation.controller;


import com.example.momentix.domain.reservation.dto.ReservationResponseDto;
import com.example.momentix.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // 공연, 장소, 시간 선택
    @PostMapping("/events/{eventId}/{eventPlaceId}/{eventTimeId}")
    public ResponseEntity<ReservationResponseDto> selectAllReservations(
            @AuthenticationPrincipal String email,
            @PathVariable Long eventId,
            @PathVariable Long eventPlaceId,
            @PathVariable Long eventTimeId) {
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

    //공연 선택
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


    //공연 장소 선택
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


    //공연 시간 선택
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


    // 좌석 선택
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
