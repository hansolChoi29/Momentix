package com.example.momentix.domain.paymenthistory.service;

import com.example.momentix.domain.common.exception.auth.AuthErrorCode;
import com.example.momentix.domain.common.exception.auth.AuthErrorException;
import com.example.momentix.domain.paymenthistory.dto.PaymentConfirmRequest;
import com.example.momentix.domain.paymenthistory.dto.PaymentCreateRequest;
import com.example.momentix.domain.paymenthistory.dto.PaymentResponse;
import com.example.momentix.domain.paymenthistory.entity.PaymentHistory;
import com.example.momentix.domain.paymenthistory.entity.PaymentStatusType;
import com.example.momentix.domain.paymenthistory.repository.PaymentHistoryRepository;
import com.example.momentix.domain.point.service.PointService;
import com.example.momentix.domain.queue.QueueService;
import com.example.momentix.domain.reservation.entity.Reservations;
import com.example.momentix.domain.reservation.repository.ReservationRepository;
import com.example.momentix.domain.ticket.dto.request.CreateTicketRequestDto;
import com.example.momentix.domain.ticket.dto.response.TicketResponseDto;
import com.example.momentix.domain.ticket.entity.Tickets;
import com.example.momentix.domain.ticket.repository.TicketRepository;
import com.example.momentix.domain.ticket.service.TicketService;
import com.example.momentix.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.momentix.domain.common.exception.reservation.ReservationErrorException;

import static com.example.momentix.domain.common.exception.reservation.ReservationErrorCode.*;

import com.example.momentix.domain.common.exception.paymenthistory.PaymentHistoryErrorException;

import static com.example.momentix.domain.common.exception.paymenthistory.PaymentHistoryCode.*;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentHistoryService {
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final ReservationRepository reservationRepository;
    private final TicketService ticketService;
    private final TicketRepository ticketRepository;
    private final PointService pointService;
    private final QueueService queueService;
    private final UserRepository userRepository;

    private Long getUserId(String email) {
        return userRepository.findBySignIn_Username(email)
                .orElseThrow(() -> new AuthErrorException(AuthErrorCode.NOT_FOUND))
                .getUserId();
    }

    @Transactional
    public PaymentResponse create(
            String email,
            PaymentCreateRequest paymentCreateRequest
    ) {
        Long userId = getUserId(email);

        Reservations reservation = reservationRepository.findById(paymentCreateRequest.getReservationId())
                .orElseThrow(() -> new ReservationErrorException(NO_RESERVATION));

        if (!reservation.getUsers().getUserId().equals(userId)) {
            throw new ReservationErrorException(NO_MY_RESERVATION);
        }

        if (paymentHistoryRepository.existsPendingByReservation(paymentCreateRequest.getReservationId())) {
            throw new ReservationErrorException(PENDING_PAYMENT);
        }

        PaymentHistory paymentHistory = PaymentHistory.create(
                paymentCreateRequest.getReservationId(),
                paymentCreateRequest.getPayer() == null ? "SELF" : paymentCreateRequest.getPayer(),
                paymentCreateRequest.getPaymentMethod() == null ? "MOCK" : paymentCreateRequest.getPaymentMethod(),
                paymentCreateRequest.getPaymentPrice(),
                paymentCreateRequest.getIdempotencyKey()
        );
        paymentHistoryRepository.save(paymentHistory);
        return PaymentResponse.of(paymentHistory);
    }

    // 결제 확정 (비관적 락 + 멱등)
    @Transactional
    public PaymentResponse confirm(
            String email,
            Long paymentId,
            PaymentConfirmRequest paymentConfirmRequest
    ) {
        Long userId = getUserId(email);
        PaymentHistory paymentHistory = paymentHistoryRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentHistoryErrorException(PAYMENT_NOT_FOUND));

        if (!paymentHistory.getReservationId().equals(paymentConfirmRequest.getReservationId())) {
            throw new PaymentHistoryErrorException(PAYMENT_RESERVATION_MISMATCH);
        }

        Reservations reservation = reservationRepository.findById(paymentConfirmRequest.getReservationId())
                .orElseThrow(() -> new ReservationErrorException(NO_RESERVATION));
        if (!reservation.getUsers().getUserId().equals(userId)) {
            throw new ReservationErrorException(NO_MY_RESERVATION);
        }

        // 1) 포인트 사용(선택) - 멱등키 고정 생성
        if (paymentConfirmRequest.getPointsToUse() > 0) {
            String useIdemKey = "PAY-" + paymentHistory.getPaymentHistoryId() + "-POINT-USE";
            pointService.use(
                    userId,
                    useIdemKey,
                    paymentConfirmRequest.getPointsToUse(),
                    "결제 시 포인트 사용",
                    paymentHistory.getPaymentHistoryId(),
                    paymentHistory.getReservationId()
            );
        }

        // 2) 이미 티켓 링크가 있으면 멱등 처리
        Optional<Long> linkedTicketIdOpt = ticketRepository.findIdByPaymentId(paymentId);
        if (linkedTicketIdOpt.isPresent()) {
            if (paymentHistory.getPaymentStatusType() == PaymentStatusType.PENDING) {
                paymentHistory.markSuccess();
                triggerPointPending(userId, paymentHistory, paymentHistory.getPaymentStatusType());
            }
            return PaymentResponse.of(paymentHistory);
        }

        // 3) 같은 예약으로 이미 발급된 티켓이 있으면 중복 방지
        boolean ticketExists = ticketRepository.existsTicketByReservationId(paymentConfirmRequest.getReservationId());
        if (ticketExists) {
            throw new ReservationErrorException(ALREADY_TICKET);
        }

        // 4) 티켓 발급
        CreateTicketRequestDto createTicketRequestDto = new CreateTicketRequestDto();
        createTicketRequestDto.setReservationId(paymentConfirmRequest.getReservationId());
        TicketResponseDto ticket = ticketService.createTicket(email, createTicketRequestDto);

        // 5) 티켓에 결제ID 링크 (FK 주인: 티켓)
        int updated = ticketRepository.linkPayment(ticket.getTicketId(), paymentHistory);
        if (updated == 0) {
            throw new PaymentHistoryErrorException(TICKET_PAYMENT_LINK_FAILED);
        }

        // 6) 결제 성공 마킹
        paymentHistory.markSuccess();

        paymentHistoryRepository.save(paymentHistory);

        // 7) 적립 예정(3%) 트리거 (멱등)
        triggerPointPending(userId, paymentHistory, paymentHistory.getPaymentStatusType());

        // 8) 예매 완료 상태
        String token = queueService.getToken(userId, reservation.getEvents().getId());
        queueService.completeQueue(reservation.getEvents().getId(), token);

        return PaymentResponse.of(paymentHistory);
    }

    // 결제 취소 (비관적 락)
    @Transactional
    public PaymentResponse cancel(String email, Long paymentId) {
        Long userId = getUserId(email);
        PaymentHistory paymentHistory = paymentHistoryRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentHistoryErrorException(PAYMENT_NOT_FOUND));

        Reservations reservation = reservationRepository.findById(paymentHistory.getReservationId())
                .orElseThrow(() -> new ReservationErrorException(NO_RESERVATION));
        if (!reservation.getUsers().getUserId().equals(userId)) {
            throw new ReservationErrorException(NO_MY_RESERVATION);
        }

        if (paymentHistory.getPaymentStatusType() == PaymentStatusType.CANCEL) {
            return PaymentResponse.of(paymentHistory);
        }

        if (paymentHistory.getPaymentStatusType() == PaymentStatusType.PENDING) {
            paymentHistory.markCancel();
            return PaymentResponse.of(paymentHistory);
        }

        if (paymentHistory.getPaymentStatusType() == PaymentStatusType.SUCCESS) {
            Optional<Long> ticketIdOpt = ticketRepository.findIdByPaymentId(paymentId);
            if (ticketIdOpt.isPresent()) {
                Long ticketId = ticketIdOpt.get();
                Tickets ticket = ticketRepository.findById(ticketId)
                        .orElseThrow(() -> new PaymentHistoryErrorException(TICKET_NOT_FOUND));
                ticket.softDelete();

                int unlinked = ticketRepository.unlinkPayment(ticketId, paymentId);
                if (unlinked == 0) {
                    throw new PaymentHistoryErrorException(TICKET_PAYMENT_LINK_FAILED);
                }
            }

            paymentHistory.markCancel();

            // (A) 이 결제로 쌓인 적립 예정 포인트는 취소
            String cancelPendingIdemKey = "PAY-" + paymentHistory.getPaymentHistoryId() + "-PEND-CANCEL";
            pointService.cancelPendingForPayment(
                    email,
                    cancelPendingIdemKey,
                    paymentHistory.getPaymentHistoryId(),
                    "결제 취소로 적립 예정 취소"
            );

            // (B) 이 결제로 사용했던 포인트가 있으면 환급
            String refundUseIdemKey = "PAY-" + paymentHistory.getPaymentHistoryId() + "-REFUND-USE";
            pointService.refundUsedPointsForPayment(
                    email,
                    refundUseIdemKey,
                    paymentHistory.getPaymentHistoryId(),
                    "결제 취소로 포인트 사용 환급"
            );

            return PaymentResponse.of(paymentHistory);
        }

        // FAILED → CANCEL 로 정리
        paymentHistory.markCancel();
        return PaymentResponse.of(paymentHistory);
    }

    //단건조회
    @Transactional
    public PaymentResponse getOne(String email, Long paymentId) {
        Long userId = getUserId(email);
        PaymentHistory paymentHistory = paymentHistoryRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentHistoryErrorException(PAYMENT_NOT_FOUND));

        Reservations reservation = reservationRepository.findById(paymentHistory.getReservationId())
                .orElseThrow(() -> new ReservationErrorException(NO_RESERVATION));

        if (!reservation.getUsers().getUserId().equals(userId)) {
            throw new PaymentHistoryErrorException(PAYMENT_FORBIDDEN);
        }
        return PaymentResponse.of(paymentHistory);
    }

    private void triggerPointPending(Long userId, PaymentHistory paymentHistory, PaymentStatusType paymentStatus) {
        BigDecimal discountedAmount = paymentHistory.getPaymentPrice(); // 필요 시 실제 할인 반영
        String idemKey = "PAY-" + paymentHistory.getPaymentHistoryId() + "-PEND-EARN";
        pointService.earnPendingByPaymentAmount(
                userId,
                idemKey,
                paymentHistory.getPaymentHistoryId(),
                paymentHistory.getReservationId(),
                discountedAmount,
                "결제 적립 예정(3%)",
                paymentStatus
        );
    }
}