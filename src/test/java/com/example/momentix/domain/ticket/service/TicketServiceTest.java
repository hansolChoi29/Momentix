package com.example.momentix.domain.ticket.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.momentix.domain.common.exception.ticket.TicketErrorException;
import com.example.momentix.domain.events.entity.EventSeat;
import com.example.momentix.domain.events.entity.eventtimes.EventTimeReserveSeat;
import com.example.momentix.domain.events.entity.eventtimes.EventTimes;
import com.example.momentix.domain.events.entity.seats.Seats;
import com.example.momentix.domain.reservation.entity.Reservations;
import com.example.momentix.domain.reservation.repository.ReservationRepository;
import com.example.momentix.domain.ticket.dto.request.CreateTicketRequestDto;
import com.example.momentix.domain.ticket.dto.request.UpdateTicketStatusRequestDto;
import com.example.momentix.domain.ticket.dto.response.TicketResponseDto;
import com.example.momentix.domain.ticket.entity.TicketStatusType;
import com.example.momentix.domain.ticket.entity.Tickets;
import com.example.momentix.domain.ticket.repository.TicketRepository;
import com.example.momentix.domain.users.entity.Users;
import com.example.momentix.domain.users.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private TicketService ticketService;

    @Mock
    private Users requester;

    @Mock
    private Users otherUser;

    @Mock
    private Reservations reservation;

    @Mock
    private EventTimeReserveSeat eventTimeReserveSeat;

    @Mock
    private EventSeat eventSeat;

    @Mock
    private Seats seat;

    @Mock
    private EventTimes eventTime;

    @Mock
    private Tickets ticket;

    /* TODO : 정상 구매 흐름 확인 / 남의 예약 훔쳐서 티켓 만드는 걸 막는 보안 테스트
       1. createTicket()
      - 내 예약으로 티켓 발행 요청 시 티켓이 정상 생성
      - 다른 사람 예약으로 티켓 발행 시 예외

      티켓 번호 생성됐는지, repository.save호출됐는지, 예외 타입 맞는지 정도
      
      2. 티켓 상태 변경 규칙 테스트
      - 티켓 상태를 CANCEL_TIKET 변경하면 정상적으로 취소 처리됨
      - 허용되지 않은 상태로 티켓 상태 변경 요청 시 예외 발생
     */

    @Test
    @DisplayName("내 예약으로 티켓 발행 요청 시 티켓이 정상 생성된다")
    void createTicket_success() {
        String email = "test@test.com";
        Long reservationId = 1L;

        CreateTicketRequestDto requestDto = new CreateTicketRequestDto();
        requestDto.setReservationId(reservationId);

        given(userRepository.findBySignIn_Username(email))
                .willReturn(Optional.of(requester));
        given(requester.getUserId()).willReturn(1L);

        given(reservationRepository.findById(reservationId))
                .willReturn(Optional.of(reservation));
        given(reservation.getUsers()).willReturn(requester);

        given(reservation.getEventTimeReserveSeat()).willReturn(eventTimeReserveSeat);
        given(eventTimeReserveSeat.getEventSeat()).willReturn(eventSeat);
        given(eventSeat.getSeats()).willReturn(seat);

        given(seat.getId()).willReturn(10L);
        given(seat.getSeatRow()).willReturn(5L);
        given(seat.getSeatCol()).willReturn(8L);

        given(reservation.getEventTimes()).willReturn(eventTime);
        given(eventTime.getId()).willReturn(100L);
        given(eventTime.getEventStartTime()).willReturn(LocalDateTime.of(2026, 4, 18, 19, 0));
        given(eventTime.getEventEndTime()).willReturn(LocalDateTime.of(2026, 4, 18, 21, 0));

        given(ticketRepository.save(any(Tickets.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        TicketResponseDto result = ticketService.createTicket(email, requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getTicketNumber()).startsWith("MOMENTIX-");
        assertThat(result.getTicketStatus()).isEqualTo(TicketStatusType.COMPLETED_PAYMENT);

        verify(ticketRepository).save(any(Tickets.class));
        verify(reservation).completeTicketIssuance();
    }

    @Test
    @DisplayName("다른 사람 예약으로 티켓 발행 요청 시 예외가 발생한다")
    void createTicket_forbidden_whenReservationOwnerIsDifferent() {
        String email = "test@test.com";
        Long reservationId = 1L;

        CreateTicketRequestDto requestDto = new CreateTicketRequestDto();
        requestDto.setReservationId(reservationId);

        given(userRepository.findBySignIn_Username(email))
                .willReturn(Optional.of(requester));
        given(requester.getUserId()).willReturn(1L);

        given(reservationRepository.findById(reservationId))
                .willReturn(Optional.of(reservation));
        given(reservation.getUsers()).willReturn(otherUser);
        given(otherUser.getUserId()).willReturn(999L);

        assertThatThrownBy(() -> ticketService.createTicket(email, requestDto))
                .isInstanceOf(TicketErrorException.class)
                .hasMessage("권한이 없습니다.");
    }

    @Test
    @DisplayName("티켓 상태를 CANCEL_TICKET으로 변경하면 정상적으로 취소 처리된다")
    void updateTicketStatus_success_whenCancelTicket() {
        String email = "test@test.com";
        Long ticketId = 1L;

        UpdateTicketStatusRequestDto requestDto = new UpdateTicketStatusRequestDto();
        requestDto.setTicketStatus(TicketStatusType.CANCEL_TICKET);

        given(userRepository.findBySignIn_Username(email))
                .willReturn(Optional.of(requester));
        given(ticketRepository.findById(ticketId))
                .willReturn(Optional.of(ticket));

        given(requester.getUserId()).willReturn(1L);
        given(ticket.getUsers()).willReturn(requester);

        ticketService.updateTicketStatus(ticketId, requestDto, email);

        verify(ticket).updateStatus(TicketStatusType.CANCEL_TICKET);
    }

    @Test
    @DisplayName("허용되지 않은 상태로 티켓 상태 변경 요청 시 예외가 발생한다")
    void updateTicketStatus_fail_whenInvalidStatus() {
        String email = "test@test.com";
        Long ticketId = 1L;

        UpdateTicketStatusRequestDto requestDto = new UpdateTicketStatusRequestDto();
        requestDto.setTicketStatus(TicketStatusType.NO_SHOW);

        given(userRepository.findBySignIn_Username(email))
                .willReturn(Optional.of(requester));
        given(ticketRepository.findById(ticketId))
                .willReturn(Optional.of(ticket));

        given(requester.getUserId()).willReturn(1L);
        given(ticket.getUsers()).willReturn(requester);

        assertThatThrownBy(() -> ticketService.updateTicketStatus(ticketId, requestDto, email))
                .isInstanceOf(TicketErrorException.class)
                .hasMessage("잘못된 티켓 상태 값입니다.");
    }
}