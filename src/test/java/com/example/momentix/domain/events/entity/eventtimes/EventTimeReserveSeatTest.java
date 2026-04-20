package com.example.momentix.domain.events.entity.eventtimes;

import com.example.momentix.domain.common.exception.event.EventErrorException;
import com.example.momentix.domain.events.entity.EventSeat;
import com.example.momentix.domain.events.entity.enums.SeatStatusType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class EventTimeReserveSeatTest {
    /* TODO : 같은 좌석을 두 사람이 잡는 사고를 막기 위한 가장 핵심 안전장치 테스트
     락을 아무리 Redis 돌려도 도메인 객체 자체가 잘못 동작하면 끝임
    - 예약 가능한 좌석에 hold를 호출하면 HOLD 변경됨
    - 이미 HOLD 상태인 좌석에 hold 호출 시 에러
    - hold된 좌석에 release를 호출하면 AVAILABLE
    * */
    private EventTimeReserveSeat createSeat(SeatStatusType status) {
        EventTimes eventTimes = Mockito.mock(EventTimes.class);
        EventSeat eventSeat = Mockito.mock(EventSeat.class);

        return EventTimeReserveSeat.builder()
                .eventTimes(eventTimes)
                .eventSeat(eventSeat)
                .seatReserveStatus(status)
                .build();
    }
    /* 
    1. @Mock은 클래스 필드에 미리 mock을 만들어두는 방식이고 
        클래스 위쪽에 둠, 여러 테스트에서 재사용하기 좋음, 의존성 많을 때
    2. Mockito.mock()은 필요한 순간에 직접 만드는 방식 
       메서드 안에서 만듦, 잠깐 쓸 때.
    */

    @Test
    @DisplayName("예약 가능한 좌석에 hold를 호출하면 HOLD 상태로 변경된다")
    void hold_changesStatusToHold_whenSeatIsAvailable() {
        EventTimeReserveSeat seat = createSeat(SeatStatusType.AVAILABLE);

        seat.hold();

        assertThat(seat.getSeatReserveStatus()).isEqualTo(SeatStatusType.HOLD);
    }

    @Test
    @DisplayName("이미 HOLD 상태인 좌석에 hold를 호출하면 예외가 발생한다")
    void hold_throwsException_whenSeatIsAlreadyHold() {
        EventTimeReserveSeat seat = createSeat(SeatStatusType.HOLD);

        assertThatThrownBy(seat::hold)
                .isInstanceOf(EventErrorException.class);
    }

    @Test
    @DisplayName("hold된 좌석에 release를 호출하면 AVAILABLE 상태로 돌아간다")
    void release_changesStatusToAvailable_whenSeatIsHold() {
        EventTimeReserveSeat seat = createSeat(SeatStatusType.HOLD);

        seat.release();

        assertThat(seat.getSeatReserveStatus()).isEqualTo(SeatStatusType.AVAILABLE);
    }
}