package com.example.momentix.domain.point.controller;


import com.example.momentix.domain.paymenthistory.entity.PaymentStatusType;
import com.example.momentix.domain.point.dto.PaymentByPaymentRequest;
import com.example.momentix.domain.point.dto.PaymentEarnByPaymentRequest;
import com.example.momentix.domain.point.dto.PointBalanceResponse;
import com.example.momentix.domain.point.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@Tag(name = "Point", description = "포인트 관련 API")
@Controller
@RequestMapping("/users/points")
public class PointController {
    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }


    @Operation(summary = "내 포인트 조회")
    @GetMapping("/me")
    public ResponseEntity<PointBalanceResponse> me(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(pointService.getMyPoints(email));
    }

    @Operation(summary = "결제 적립 예정 추가", description = "결제 성공 시 3% 적립 예정으로 쌓기")
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

    @Operation(summary = "적립 예정 확정", description = "환불 불가 시점에 예정 포인트를 잔액으로 전환")
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

    @Operation(summary = "적립 예정 취소", description = "결제 취소 시 적립 예정 포인트 취소")
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

    @Operation(summary = "사용 포인트 환급", description = "결제 취소 시 사용했던 포인트 복구")
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
