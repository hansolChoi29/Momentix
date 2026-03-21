package com.example.momentix.domain.paymenthistory.controller;

import com.example.momentix.domain.paymenthistory.dto.PaymentConfirmRequest;
import com.example.momentix.domain.paymenthistory.dto.PaymentCreateRequest;
import com.example.momentix.domain.paymenthistory.dto.PaymentResponse;
import com.example.momentix.domain.paymenthistory.service.PaymentHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentHistoryController {
    private final PaymentHistoryService paymentHistoryService;

    public PaymentHistoryController(PaymentHistoryService paymentHistoryService) {
        this.paymentHistoryService = paymentHistoryService;
    }

    //결제 대기
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

    //결제 확정
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

    //결제 조회
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

    //결제 삭제
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
