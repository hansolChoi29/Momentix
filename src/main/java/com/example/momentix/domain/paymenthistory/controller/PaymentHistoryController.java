package com.example.momentix.domain.paymenthistory.controller;

import com.example.momentix.domain.paymenthistory.dto.PaymentConfirmRequest;
import com.example.momentix.domain.paymenthistory.dto.PaymentCreateRequest;
import com.example.momentix.domain.paymenthistory.dto.PaymentResponse;
import com.example.momentix.domain.paymenthistory.service.PaymentHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Payment", description = "결제 관련 API")
@RestController
@RequestMapping("/payment")
public class PaymentHistoryController {
    private final PaymentHistoryService paymentHistoryService;

    public PaymentHistoryController(PaymentHistoryService paymentHistoryService) {
        this.paymentHistoryService = paymentHistoryService;
    }

    @Operation(summary = "결제 생성", description = "결제 대기(PENDING) 상태로 생성")
    @PostMapping
    public ResponseEntity<PaymentResponse> create(
            @AuthenticationPrincipal String email,
            @RequestBody PaymentCreateRequest paymentCreateReq
    ) {
        return ResponseEntity.ok(paymentHistoryService.create(
                email,
                paymentCreateReq)
        );
    }

    @Operation(summary = "결제 확정", description = "티켓 발급 및 포인트 적립 예정 처리")
    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<PaymentResponse> confirm(
            @AuthenticationPrincipal String email,
            @PathVariable Long paymentId,
            @RequestBody PaymentConfirmRequest paymentConfirmReq
    ) {
        return ResponseEntity.ok(paymentHistoryService.confirm(
                email,
                paymentId,
                paymentConfirmReq)
        );
    }

    @Operation(summary = "결제 단건 조회")
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getOne(
            @AuthenticationPrincipal String email,
            @PathVariable Long paymentId
    ) {
        return ResponseEntity.ok(paymentHistoryService.getOne(
                email,
                paymentId)
        );
    }

    @Operation(summary = "결제 취소", description = "티켓 소프트딜리트 및 포인트 환급 처리")
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancel(
            @AuthenticationPrincipal String email,
            @PathVariable Long paymentId
    ) {
        return ResponseEntity.ok(paymentHistoryService.cancel(
                email,
                paymentId)
        );
    }
}
