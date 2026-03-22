package com.example.momentix.domain.events.controller;

import com.example.momentix.domain.events.dto.request.PlacesRequestDto;
import com.example.momentix.domain.events.dto.response.BaseSeatResponseDto;
import com.example.momentix.domain.events.dto.response.PlaceResponseDto;
import com.example.momentix.domain.events.service.PlaceService;
import com.example.momentix.domain.events.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Place", description = "공연장 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/places")
public class PlaceController {

    public final PlaceService placeService;
    public final SeatService seatService;

    @Operation(summary = "공연장 등록")
    @PostMapping
    public ResponseEntity<PlaceResponseDto> createPlace(@RequestBody PlacesRequestDto placesRequest) {
        return new ResponseEntity<>(new PlaceResponseDto(placeService.createPlace(placesRequest)), HttpStatus.CREATED);
    }

    @Operation(summary = "기본 좌석 배치 등록", description = "CSV 파일로 좌석 배치 업로드")
    @PostMapping("/base-seat")
    public ResponseEntity<List<BaseSeatResponseDto>> createBaseSeat(
            @RequestPart("file") MultipartFile baseSeatFile,
            @RequestPart("request") PlacesRequestDto placesRequest) {
        List<BaseSeatResponseDto> response = seatService.createBaseSeat(baseSeatFile, placesRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

}
