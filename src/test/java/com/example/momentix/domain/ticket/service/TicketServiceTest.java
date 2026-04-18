package com.example.momentix.domain.ticket.service;

import static org.junit.jupiter.api.Assertions.*;

class TicketServiceTest {
    /* TODO : 정상 구매 흐름 확인 / 남의 예약 훔쳐서 티켓 만드는 걸 막는 보안 테스트
       1. createTicket()
      - 내 예약으로 티켓 발행 요청 시 티켓이 정상 생성
      - 다른 사람 예약으로 티켓 발행 시 예외

      티켓 번호 생성됐는지, repository.save호출됐는지, 예외 타입 맞는지 정도
      
      2. 티켓 상태 변경 규칙 테스트
      - 티켓 상태를 CANCEL_TIKET 변경하면 정상적으로 취소 처리됨
      - 허용되지 않은 상태로 티켓 상태 변경 요청 시 예외 발생
     */
}