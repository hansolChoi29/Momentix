package com.example.momentix.domain.reservation.entity;

import com.example.momentix.domain.events.entity.EventPlace;
import com.example.momentix.domain.events.entity.Events;
import com.example.momentix.domain.events.entity.eventtimes.EventTimeReserveSeat;
import com.example.momentix.domain.events.entity.eventtimes.EventTimes;
import com.example.momentix.domain.users.entity.Users;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class ReservationsTest {
    /* TODO : 상태값만 보지 말고 null 초기화까지 같이 보기

     - 장소 선택 시 상태가 SELECT_PLACE 변경되고 기존 시간과 좌석은 초기화됨

     - 시간 선택 시 SELECT_TIME 변경되고 기존 좌석은 초기화됨

     - 좌석 선택 시 SELECT_SEAT 변경됨

     - 티켓 발급 완료 시 상태가 COMPLETED_TICKET 변경됨
     */
    @Mock
    private Users user;

    @Mock
    private Events event;

    @Mock
    private EventPlace place;

    @Mock
    private EventTimes eventTime;

    @Mock
    private EventTimeReserveSeat seat;

    @Test
    @DisplayName("장소선택, SELECT_PLACE. 기존시간과 좌석은 초기화")
    void placeUpdate_SELECT_PLACE() {
        Reservations reservation = Reservations.builder()
                .users(user)
                .events(event)
                .reservationStatusType(ReservationStatusType.DRAFT)
                .build();

        reservation.selectEventPlace(place);

        assertThat(reservation.getReservationStatusType()).isEqualTo(ReservationStatusType.SELECT_PLACE);
    }

    @Test
    @DisplayName("시간 선택 시 상태가 SELECT_TIME으로 변경되고 기존 좌석은 초기화")
    void selectEventTime_changesStatusAndClearsSeat() {
        Reservations reservation = Reservations.builder()
                .users(user)
                .events(event)
                .reservationStatusType(ReservationStatusType.DRAFT)
                .build();

        reservation.selectEventPlace(place);
        reservation.selectEventSeat(seat);

        reservation.selectEventTime(eventTime);

        assertThat(reservation.getReservationStatusType()).isEqualTo(ReservationStatusType.SELECT_TIME);
        assertThat(reservation.getEventTimes()).isEqualTo(eventTime);
        assertThat(reservation.getEventTimeReserveSeat()).isNull();
    }

    @Test
    @DisplayName("좌석 선택 시 상태가 SELECT_SEAT으로 변경")
    void selectEventSeat_changesStatus() {
        Reservations reservation = Reservations.builder()
                .users(user)
                .events(event)
                .reservationStatusType(ReservationStatusType.DRAFT)
                .build();

        reservation.selectEventPlace(place);
        reservation.selectEventTime(eventTime);

        reservation.selectEventSeat(seat);

        assertThat(reservation.getReservationStatusType()).isEqualTo(ReservationStatusType.SELECT_SEAT);
        assertThat(reservation.getEventTimeReserveSeat()).isEqualTo(seat);
    }

    @Test
    @DisplayName("티켓 발급 완료 시 상태가 COMPLETED_TICKET으로 변경")
    void completeTicketIssuance_changesStatus() {
        Reservations reservation = Reservations.builder()
                .users(user)
                .events(event)
                .reservationStatusType(ReservationStatusType.SELECT_SEAT)
                .build();

        reservation.completeTicketIssuance();

        assertThat(reservation.getReservationStatusType()).isEqualTo(ReservationStatusType.COMPLETED_TICKET);
    }
}