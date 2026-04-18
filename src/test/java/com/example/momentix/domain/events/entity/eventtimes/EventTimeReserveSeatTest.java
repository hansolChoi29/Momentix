package com.example.momentix.domain.events.entity.eventtimes;

import static org.junit.jupiter.api.Assertions.*;

class EventTimeReserveSeatTest {
    /* TODO : 같은 좌석을 두 사람이 잡는 사고를 막기 위한 가장 핵심 안전장치 테스트
     락을 아무리 Redis 돌려도 도메인 객체 자체가 잘못 동작하면 끝임
    - 예약 가능한 좌석에 hold를 호출하면 HOLD 변경됨
    - 이미 HOLD 상태인 좌석에 hold 호출 시 에러
    - hold된 좌석에 release를 호출하면 AVAILABLE
    * */
}