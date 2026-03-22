package com.example.momentix.domain.point.service;

import com.example.momentix.domain.common.exception.auth.AuthErrorCode;
import com.example.momentix.domain.common.exception.auth.AuthErrorException;
import com.example.momentix.domain.common.exception.paymenthistory.PaymentHistoryErrorException;

import static com.example.momentix.domain.common.exception.paymenthistory.PaymentHistoryCode.*;

import com.example.momentix.domain.common.exception.point.PointErrorException;
import com.example.momentix.domain.common.exception.reservation.ReservationErrorException;
import com.example.momentix.domain.paymenthistory.entity.PaymentHistory;
import com.example.momentix.domain.paymenthistory.entity.PaymentStatusType;
import com.example.momentix.domain.paymenthistory.repository.PaymentHistoryRepository;
import com.example.momentix.domain.point.dto.PointBalanceResponse;
import com.example.momentix.domain.point.entity.PointLedger;
import com.example.momentix.domain.point.entity.PointOperationType;
import com.example.momentix.domain.point.entity.Points;
import com.example.momentix.domain.point.repository.PointLedgerRepository;
import com.example.momentix.domain.point.repository.PointsRepository;
import com.example.momentix.domain.reservation.entity.Reservations;
import com.example.momentix.domain.reservation.repository.ReservationRepository;
import com.example.momentix.domain.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import static com.example.momentix.domain.common.exception.reservation.ReservationErrorCode.*;
import static com.example.momentix.domain.common.exception.point.PointCode.*;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PointService {
    private final PointsRepository pointsRepository;
    private final PointLedgerRepository ledgerRepository;
    private final PointsPolicyService policyService;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    private Long getUserId(String email) {
        return userRepository.findBySignIn_Username(email)
                .orElseThrow(() -> new AuthErrorException(AuthErrorCode.NOT_FOUND))
                .getUserId();
    }

    public PointBalanceResponse getMyPoints(String email) {
        return getMyPoints(getUserId(email));
    }

    public PointBalanceResponse earnPendingByPaymentAmount(
            String email, String idempotencyKey, Long paymentId,
            Long reservationId, BigDecimal discountedAmount,
            String reason, PaymentStatusType paymentStatusType) {
        return earnPendingByPaymentAmount(getUserId(email), idempotencyKey,
                paymentId, reservationId, discountedAmount, reason, paymentStatusType);
    }

    public PointBalanceResponse releasePendingByPayment(
            String email, String idempotencyKey, Long paymentId, String reason) {
        return releasePendingByPayment(getUserId(email), idempotencyKey, paymentId, reason);
    }

    public PointBalanceResponse cancelPendingForPayment(
            String email, String idempotencyKey, Long paymentId, String reason) {
        return cancelPendingForPayment(getUserId(email), idempotencyKey, paymentId, reason);
    }

    public PointBalanceResponse refundUsedPointsForPayment(
            String email, String idempotencyKey, Long paymentId, String reason) {
        return refundUsedPointsForPayment(getUserId(email), idempotencyKey, paymentId, reason);
    }

    public PointBalanceResponse getMyPoints(Long userId) {
        Points points = pointsRepository.findByUserId(userId)
                .orElseGet(() -> pointsRepository.save(new Points(userId, 0L, 0L)));
        return snapshot(points);
    }

    @Transactional
    public PointBalanceResponse use(
            Long userId, String idempotencyKey, long amount,
            String reason, Long paymentId, Long reservationId) {
        requirePositive(amount);
        if (alreadyDone(userId, idempotencyKey)) return getMyPoints(userId);

        Points points = lockRow(userId);
        if (points.getPointBalance() < amount) {
            throw new PointErrorException(INSUFFICIENT_POINTS);
        }
        points.decreaseBalance(amount);
        writeLedger(userId, idempotencyKey, PointOperationType.USE, -amount, reason, paymentId, reservationId);
        return snapshot(points);
    }

    @Transactional
    public PointBalanceResponse earnPending(
            Long userId, String idempotencyKey, long amount,
            String reason, Long paymentId, Long reservationId) {
        requirePositive(amount);
        if (alreadyDone(userId, idempotencyKey)) return getMyPoints(userId);

        Points points = lockRow(userId);
        points.increasePending(amount);
        writeLedger(userId, idempotencyKey, PointOperationType.PENDING_EARN, amount, reason, paymentId, reservationId);
        return snapshot(points);
    }

    @Transactional
    public PointBalanceResponse releasePending(
            Long userId, String idempotencyKey, long amount,
            String reason, Long paymentId, Long reservationId) {
        requirePositive(amount);
        if (alreadyDone(userId, idempotencyKey)) return getMyPoints(userId);

        Points points = lockRow(userId);
        if (points.getPointPending() < amount) {
            throw new PointErrorException(INSUFFICIENT_PENDING_POINTS);
        }
        points.decreasePending(amount);
        points.increaseBalance(amount);
        writeLedger(userId, idempotencyKey, PointOperationType.PENDING_RELEASE, amount, reason, paymentId, reservationId);
        return snapshot(points);
    }

    @Transactional
    public PointBalanceResponse earnPendingByPaymentAmount(
            Long userId,
            String idempotencyKey,
            Long paymentId,
            Long reservationId,
            BigDecimal discountedAmount,
            String reason,
            PaymentStatusType paymentStatusType
    ) {
        if (alreadyDone(userId, idempotencyKey)) return getMyPoints(userId);

        verifyPaymentOwnershipAndMatch(userId, paymentId, reservationId);
        if (paymentStatusType != PaymentStatusType.SUCCESS) {
            throw new PaymentHistoryErrorException(PAYMENT_NOT_FOUND);
        }
        if (ledgerRepository.existsByUserIdAndPointOperationTypeAndRelatedPaymentId(
                userId, PointOperationType.PENDING_EARN, paymentId)) {
            return getMyPoints(userId);
        }

        long earn = policyService.calculateEarnPoints(discountedAmount);
        return earnPending(
                userId,
                idempotencyKey,
                earn, reason,
                paymentId,
                reservationId
        );
    }

    @Transactional
    public PointBalanceResponse releasePendingByPayment(
            Long userId,
            String idempotencyKey,
            Long paymentId,
            String reason
    ) {
        verifyPaymentOwnership(userId, paymentId);
        if (alreadyDone(userId, idempotencyKey)) return getMyPoints(userId);

        if (ledgerRepository.existsByUserIdAndPointOperationTypeAndRelatedPaymentId(
                userId, PointOperationType.PENDING_RELEASE, paymentId)) {
            return getMyPoints(userId);
        }

        long pending = ledgerRepository.sumPendingEarnByPayment(userId, paymentId);
        if (pending <= 0) return getMyPoints(userId);

        return releasePending(
                userId,
                idempotencyKey,
                pending,
                reason,
                paymentId,
                null
        );
    }

    @Transactional
    public PointBalanceResponse cancelPendingForPayment(
            Long userId,
            String idempotencyKey,
            Long paymentId,
            String reason
    ) {
        if (alreadyDone(userId, idempotencyKey)) return getMyPoints(userId);

        verifyPaymentOwnership(userId, paymentId);

        long pendingAmount = ledgerRepository.sumPendingEarnByPayment(userId, paymentId);
        if (pendingAmount <= 0) return getMyPoints(userId);

        Points points = lockRow(userId);
        if (points.getPointPending() < pendingAmount) pendingAmount = points.getPointPending();
        points.decreasePending(pendingAmount);
        writeLedger(
                userId,
                idempotencyKey,
                PointOperationType.PENDING_CANCEL,
                -pendingAmount,
                reason,
                paymentId,
                null
        );
        return snapshot(points);
    }

    @Transactional
    public PointBalanceResponse refundUsedPointsForPayment(
            Long userId,
            String idempotencyKey,
            Long paymentId,
            String reason
    ) {
        if (alreadyDone(userId, idempotencyKey)) return getMyPoints(userId);

        verifyPaymentOwnership(userId, paymentId);

        long used = Math.abs(ledgerRepository.sumAmountByPaymentAndOp(userId, paymentId, PointOperationType.USE));
        if (used <= 0) return getMyPoints(userId);

        Points points = lockRow(userId);
        points.increaseBalance(used);
        writeLedger(userId, idempotencyKey, PointOperationType.REFUND_USE, used, reason, paymentId, null);
        return snapshot(points);
    }

    private void requirePositive(long amount) {
        if (amount <= 0) throw new PointErrorException(INVALID_POINT_AMOUNT);
    }

    private boolean alreadyDone(
            Long userId,
            String idempotencyKey
    ) {
        if (idempotencyKey == null || idempotencyKey.isEmpty())
            throw new PaymentHistoryErrorException(DUPLEICATED_REQUEST);
        return ledgerRepository.existsByUserIdAndIdempotencyKey(userId, idempotencyKey);
    }

    private Points lockRow(Long userId) {
        Optional<Points> opt = pointsRepository.findForUpdate(userId);
        return opt.orElseGet(() -> pointsRepository.save(new Points(userId, 0L, 0L)));
    }

    private void writeLedger(
            Long userId,
            String idempotencyKey,
            PointOperationType type,
            long amount,
            String reason,
            Long paymentId,
            Long reservationId
    ) {
        try {
            ledgerRepository.save(new PointLedger(
                    userId,
                    idempotencyKey,
                    type,
                    amount,
                    reason,
                    paymentId,
                    reservationId)
            );
        } catch (DataIntegrityViolationException ignored) {
        }
    }

    private PointBalanceResponse snapshot(Points points) {
        return new PointBalanceResponse(
                points.getUserId(),
                points.getPointBalance(),
                points.getPointPending()
        );
    }

    private PaymentHistory verifyPaymentOwnership(
            Long userId,
            Long paymentId
    ) {
        PaymentHistory payment = paymentHistoryRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentHistoryErrorException(PAYMENT_NO_SUCCESS));

        Reservations reservation = reservationRepository.findById(payment.getReservationId())
                .orElseThrow(() -> new ReservationErrorException(NO_RESERVATION));

        if (!reservation.getUsers().getUserId().equals(userId)) {
            throw new PaymentHistoryErrorException(PAYMENT_FORBIDDEN);
        }
        return payment;
    }

    private PaymentHistory verifyPaymentOwnershipAndMatch(
            Long userId,
            Long paymentId,
            Long reservationId
    ) {
        PaymentHistory payment = verifyPaymentOwnership(userId, paymentId);
        if (reservationId != null && !payment.getReservationId().equals(reservationId)) {
            throw new PaymentHistoryErrorException(PAYMENT_RESERVATION_MISMATCH);
        }
        return payment;
    }
}
