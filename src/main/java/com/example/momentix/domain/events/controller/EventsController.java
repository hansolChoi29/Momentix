package com.example.momentix.domain.events.controller;

import com.example.momentix.domain.events.dto.request.CreateEventsRequestDto;
import com.example.momentix.domain.events.dto.request.UpdateBaseEventRequestDto;
import com.example.momentix.domain.events.dto.response.AllReadEventsResponseDto;
import com.example.momentix.domain.events.dto.response.EventsResponseDto;
import com.example.momentix.domain.events.dto.response.ReadEventResponseDto;
import com.example.momentix.domain.events.service.EventsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Event", description = "공연 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventsController {

    private final EventsService eventsService;

    @Operation(summary = "공연 등록", description = "HOST 권한 필요")
    @PostMapping()
    public ResponseEntity<EventsResponseDto> createEvent(@RequestBody CreateEventsRequestDto requestDto) {
        EventsResponseDto response = eventsService.createEvent(requestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "공연 전체 조회")
    @GetMapping()
    public ResponseEntity<Page<AllReadEventsResponseDto>> readAllEvents(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AllReadEventsResponseDto> response = eventsService.allReadEvents(pageable);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "공연 수정")
    @PutMapping("/{eventId}")
    public ResponseEntity<Void> updateBaseEvent(@PathVariable Long eventId, @RequestBody UpdateBaseEventRequestDto requestDto) {
        eventsService.updateEvent(eventId, requestDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "공연 단건 조회")
    @GetMapping("/{eventId}/{placeId}")
    public ResponseEntity<ReadEventResponseDto> readEvent(@PathVariable Long eventId, @PathVariable Long placeId) {
        ReadEventResponseDto response = eventsService.readEvent(eventId, placeId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "공연 삭제", description = "ADMIN 권한 필요")
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        eventsService.deleteEvent(eventId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
