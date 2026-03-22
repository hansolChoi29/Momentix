package com.example.momentix.domain.events.controller;

import com.example.momentix.domain.events.dto.response.PartRowColSeatResponseDto;
import com.example.momentix.domain.events.dto.response.SeatResponseDto;
import com.example.momentix.domain.events.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Seat", description = "좌석 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class SeatController {
    private final SeatService seatService;

    @Operation(summary = "공연 좌석 등록", description = "CSV 파일로 공연별 좌석 등록")
    @PostMapping("/{eventId}/{placeId}/seats")
    public ResponseEntity<List<SeatResponseDto>> createSeat(
            @RequestPart("file") MultipartFile seatFile,
            @PathVariable Long placeId,
            @PathVariable Long eventId) {
        return new ResponseEntity<>(seatService.createSeat(seatFile, placeId, eventId), HttpStatus.CREATED);
    }

    @Operation(summary = "좌석 조회", description = "파트/행/열 조건으로 필터링 가능")
    @GetMapping("/{eventId}/{placeId}/event-time/{eventTimeId}")
    public ResponseEntity<Page<PartRowColSeatResponseDto>> readPartSeats(
            @PathVariable Long eventId,
            @PathVariable Long placeId,
            @PathVariable Long eventTimeId,
            @RequestParam(required = false) Long partId,
            @RequestParam(required = false) Long rowId,
            @RequestParam(required = false) Long colId,
            @PageableDefault Pageable pageable) {

        return new ResponseEntity<>(seatService.readSeatsPart(
                eventId, placeId, eventTimeId, partId, rowId, colId, pageable), HttpStatus.OK);
    }

    @Operation(summary = "좌석 수정", description = "CSV 파일로 좌석 정보 수정")
    @PatchMapping("/{eventId}/{placeId}/seats")
    public ResponseEntity<Void> updateSeat(
            @RequestPart("file") MultipartFile seatFile,
            @PathVariable Long eventId,
            @PathVariable Long placeId) {
        seatService.updateSeat(seatFile, eventId, placeId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "좌석 삭제", description = "CSV 파일로 삭제할 좌석 지정")
    @DeleteMapping("/{placeId}/seats")
    public ResponseEntity<Void> softDeleteSeats(
            @RequestPart("file") MultipartFile deleteFile,
            @PathVariable Long placeId) {
        seatService.deleteSeat(deleteFile, placeId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

//    // 좌석 선점을 위한 낙관적 락 API 엔드포인트
//    @PostMapping("/optimistic/event-times/{eventTimeId}/seats/{eventSeatId}/select")
//    public ResponseEntity<String> selectSeat(
//            @PathVariable Long eventTimeId,
//            @PathVariable Long eventSeatId) {
//
//        seatService.selectSeatWithOptimisticLock(eventTimeId, eventSeatId);
//        return ResponseEntity.ok("좌석 선점에 성공했습니다.");
//    }
//
//    // Redis 분산 락 API 엔드포인트
//    @PostMapping("/redis/event-times/{eventTimeId}/seats/{eventSeatId}/select")
//    public ResponseEntity<String> selectSeatWithRedis(
//            @PathVariable Long eventTimeId,
//            @PathVariable Long eventSeatId) {
//
//        seatService.selectSeatWithRedisLock(eventTimeId, eventSeatId);
//        return ResponseEntity.ok("좌석 선점에 성공했습니다. (Redis Lock)");
//    }
//
//    // 분산 락 + 낙관적 락 API 엔드포인트
//    @PostMapping("/redis-optimistic/event-times/{eventTimeId}/seats/{eventSeatId}/select")
//    public ResponseEntity<String> selectSeatWithRedisAndOptimistic(
//            @PathVariable Long eventTimeId,
//            @PathVariable Long eventSeatId) {
//
//        seatService.selectSeatWithRedisAndOptimisticLock(eventTimeId, eventSeatId);
//        return ResponseEntity.ok("좌석 선점에 성공했습니다. (Redis + Optimistic Lock)");
//    }
}
