package com.example.momentix.domain.point.controller;


import com.example.momentix.domain.paymenthistory.entity.PaymentStatusType;
import com.example.momentix.domain.point.dto.PaymentByPaymentRequest;
import com.example.momentix.domain.point.dto.PaymentEarnByPaymentRequest;
import com.example.momentix.domain.point.dto.PointBalanceResponse;
import com.example.momentix.domain.point.service.PointService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users/points")
public class PointController {
    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    // 내 포인트 조회
    @GetMapping("/me")
    public ResponseEntity<PointBalanceResponse> me(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(pointService.getMyPoints(email));
    }

    @PostMapping("/pending/earn-by-payment")
    public ResponseEntity<PointBalanceResponse> earnPendingByPayment(
            @AuthenticationPrincipal String email,
            @RequestBody PaymentEarnByPaymentRequest paymentEarnByPaymentRequest
    ) {
        return ResponseEntity.ok(pointService.earnPendingByPaymentAmount(
                email,
                paymentEarnByPaymentRequest.getIdempotencyKey(),
                paymentEarnByPaymentRequest.getPaymentId(),
                paymentEarnByPaymentRequest.getReservationId(),
                paymentEarnByPaymentRequest.getDiscountedAmount(),
                "결제 적립 예정(3%)",
                PaymentStatusType.SUCCESS)
        );
    }

    @PostMapping("/pending/release-by-payment")
    public ResponseEntity<PointBalanceResponse> releasePendingByPayment(
            @AuthenticationPrincipal String email,
            @RequestBody PaymentByPaymentRequest paymentByPaymentRequest
    ) {
        return ResponseEntity.ok(pointService.releasePendingByPayment(
                email,
                paymentByPaymentRequest.getIdempotencyKey(),
                paymentByPaymentRequest.getPaymentId(),
                "환불 불가 시점 적립 확정")
        );
    }

    @PostMapping("/pending/cancel-by-payment")
    public ResponseEntity<PointBalanceResponse> cancelPendingByPayment(
            @AuthenticationPrincipal String email,
            @RequestBody PaymentByPaymentRequest paymentByPaymentRequest
    ) {
        return ResponseEntity.ok(pointService.cancelPendingForPayment(
                email,
                paymentByPaymentRequest.getIdempotencyKey(),
                paymentByPaymentRequest.getPaymentId(),
                "결제 취소로 적립 예정 취소")
        );
    }

    @PostMapping("/refund-used-by-payment")
    public ResponseEntity<PointBalanceResponse> refundUsedByPayment(
            @AuthenticationPrincipal String email,
            @RequestBody PaymentByPaymentRequest paymentByPaymentRequest
    ) {
        return ResponseEntity.ok(pointService.refundUsedPointsForPayment(
                email,
                paymentByPaymentRequest.getIdempotencyKey(),
                paymentByPaymentRequest.getPaymentId(),
                "결제 취소로 포인트 사용 환급")
        );
    }
}
