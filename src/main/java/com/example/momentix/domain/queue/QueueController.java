package com.example.momentix.domain.queue;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/queue")
public class QueueController {

    private final QueueService queueService;

    @PostMapping("/{eventId}")
    public ResponseEntity<String> addQueue(
            @AuthenticationPrincipal String email,
            @PathVariable Long eventId,
            HttpSession session) {
        String token = queueService.addQueue(email, session.getId(), eventId);
        return ResponseEntity.ok("예매 가능/대기 상태로 진입" + token);
    }

    @PostMapping("/rank/{eventId}")
    public ResponseEntity<String> rankQueue(
            @PathVariable Long eventId,
            @RequestParam String token
    ) {
        queueService.rankAlarmQueue(eventId, token);
        return ResponseEntity.ok("대기열 순위 확인");
    }

    @PostMapping("/process/{eventId}")
    public ResponseEntity<String> processQueue(@PathVariable Long eventId) {
        queueService.processQueue(eventId);
        return ResponseEntity.ok("대기열 변경 확인");
    }

    @PostMapping("/end/{eventId}")
    public ResponseEntity<String> endQueue(
            @PathVariable Long eventId,
            @RequestParam String token
    ) {
        queueService.completeQueue(eventId, token);
        return ResponseEntity.ok("예매 완료, 대기열에서 삭제");
    }
}
