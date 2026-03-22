package com.example.momentix.domain.events.controller;

import com.example.momentix.domain.events.service.EventImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "Event Image", description = "공연 이미지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/events/{eventId}/images")
public class EventImageController {

    private final EventImageService eventImageService;

    @Operation(summary = "공연 이미지 업로드", description = "imageType: poster 또는 detail")
    @PostMapping("/{imageType}")
    public ResponseEntity<String> uploadImage(
            @PathVariable Long eventId,
            @PathVariable String imageType,
            @RequestParam("imageFile") MultipartFile file) throws IOException {

        String imageUrl = eventImageService.uploadImage(eventId, imageType, file);
        return ResponseEntity.ok("업로드 성공: " + imageUrl);
    }
}