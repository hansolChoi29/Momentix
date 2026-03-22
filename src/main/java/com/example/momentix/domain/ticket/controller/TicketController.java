package com.example.momentix.domain.ticket.controller;

import com.example.momentix.domain.ticket.dto.request.CreateTicketRequestDto;
import com.example.momentix.domain.ticket.dto.request.UpdateTicketStatusRequestDto;
import com.example.momentix.domain.ticket.dto.response.TicketResponseDto;
import com.example.momentix.domain.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Ticket", description = "티켓 관련 API")
@RestController
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "티켓 발급", description = "결제 확정 후 티켓 생성")
    @PostMapping("/tickets")
    public ResponseEntity<TicketResponseDto> createTicket(@RequestBody CreateTicketRequestDto requestDto) {
        TicketResponseDto response = ticketService.createTicket(requestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "내 티켓 목록 조회")
    @GetMapping("/tickets")
    public ResponseEntity<Page<TicketResponseDto>> getMyTickets(
            @AuthenticationPrincipal String email,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<TicketResponseDto> response = ticketService.getMyTickets(email, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "티켓 단건 조회")
    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<TicketResponseDto> getTicket(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal String email
    ) {
        TicketResponseDto response = ticketService.getTicket(ticketId, email);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "티켓 상태 변경", description = "CANCEL_TICKET 상태로 변경 가능")
    @PatchMapping("/tickets/{ticketId}")
    public ResponseEntity<String> updateTicketStatus(
            @PathVariable Long ticketId,
            @RequestBody UpdateTicketStatusRequestDto requestDto,
            @AuthenticationPrincipal String email
    ) {
        ticketService.updateTicketStatus(ticketId, requestDto, email);
        return ResponseEntity.ok("예매 상태가 성공적으로 변경되었습니다.");
    }

    @Operation(summary = "티켓 삭제", description = "ADMIN만 가능")
    @DeleteMapping("/tickets/{ticketId}")
    public ResponseEntity<Void> deleteTicket(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal String email
    ) {
        ticketService.softDeleteTicketByAdmin(ticketId, email);
        return ResponseEntity.noContent().build();
    }

}