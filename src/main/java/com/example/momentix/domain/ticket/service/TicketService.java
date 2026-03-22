package com.example.momentix.domain.ticket.service;

import com.example.momentix.domain.auth.entity.RoleType;
import com.example.momentix.domain.common.exception.ticket.TicketErrorException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.example.momentix.domain.common.exception.ticket.TicketCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final ReservationRepository reservationRepository;

    private Users getUser(String email) {
        return userRepository.findBySignIn_Username(email)
                .orElseThrow(() -> new TicketErrorException(FORBIDDEN));
    }

    @Transactional
    public TicketResponseDto createTicket(String email, CreateTicketRequestDto requestDto) {
        Users requester = getUser(email);

        Reservations reservation = reservationRepository.findById(requestDto.getReservationId())
                .orElseThrow(() -> new TicketErrorException(RESERVATION_NOT_FOUND));

        if (!reservation.getUsers().getUserId().equals(requester.getUserId())) {
            throw new TicketErrorException(FORBIDDEN);
        }

        return issueTicket(reservation);
    }

    private TicketResponseDto issueTicket(Reservations reservation) {
        String ticketNumber = "MOMENTIX-" + UUID.randomUUID().toString().toUpperCase().substring(0, 13);

        Tickets ticket = new Tickets(
                reservation.getUsers(),
                reservation.getEventTimeReserveSeat().getEventSeat().getSeats(),
                reservation.getEventTimes(),
                ticketNumber
        );

        Tickets savedTicket = ticketRepository.save(ticket);
        reservation.completeTicketIssuance();

        log.info("test{},--{}", savedTicket.getTicketId(), savedTicket.getTicketNumber());

        return new TicketResponseDto(savedTicket);
    }

    // 내 티켓 내역 전체 조회
    public Page<TicketResponseDto> getMyTickets(
            String email,
            Pageable pageable
    ) {
        Users user = getUser(email);
        Page<Tickets> ticketPage = ticketRepository.findByUsersAndIsDeletedFalse(user, pageable);

        return ticketPage.map(TicketResponseDto::new);
    }

    // 내 티켓 내역 단건 조회
    public TicketResponseDto getTicket(
            Long ticketId,
            String email
    ) {
        Users user = getUser(email);
        Tickets ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketErrorException(RESERVATION_NOT_FOUND));

        if (!ticket.getUsers().getUserId().equals(user.getUserId())) {
            throw new TicketErrorException(FORBIDDEN);
        }

        return new TicketResponseDto(ticket);
    }

    // 티켓 결제 취소
    @Transactional
    public void updateTicketStatus(
            Long ticketId,
            UpdateTicketStatusRequestDto requestDto,
            String email
    ) {
        Users user = getUser(email);
        Tickets ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketErrorException(RESERVATION_NOT_FOUND));

        if (!ticket.getUsers().getUserId().equals(user.getUserId())) {
            throw new TicketErrorException(FORBIDDEN);
        }

        if (requestDto.getTicketStatus() != TicketStatusType.CANCEL_TICKET) {
            throw new TicketErrorException(INVALID_TICKET_STATUS);
        }

        ticket.updateStatus(requestDto.getTicketStatus());
    }

    // 티켓 내역 삭제
    @Transactional
    public void softDeleteTicketByAdmin(
            Long ticketId,
            String email
    ) {
        Users adminUser = getUser(email);
        if (adminUser.getRole() != RoleType.ADMIN) {
            throw new TicketErrorException(FORBIDDEN);
        }

        Tickets ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketErrorException(RESERVATION_NOT_FOUND));

        ticket.softDelete();
    }
}