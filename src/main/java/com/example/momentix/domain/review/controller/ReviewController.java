package com.example.momentix.domain.review.controller;

import com.example.momentix.domain.review.dto.request.CreateReviewRequestDto;
import com.example.momentix.domain.review.dto.request.UpdateReviewRequestDto;
import com.example.momentix.domain.review.dto.response.ReviewResponseDto;
import com.example.momentix.domain.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Review", description = "리뷰 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성", description = "해당 공연을 예매한 사용자만 작성 가능")
    @PostMapping("/events/{eventId}")
    public ResponseEntity<ReviewResponseDto> createReview(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateReviewRequestDto requestDto,
            @AuthenticationPrincipal String email
    ) {
        ReviewResponseDto response = reviewService.createReview(
                eventId,
                requestDto,
                email
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "리뷰 목록 조회")
    @GetMapping("/events/{eventId}")
    public ResponseEntity<Page<ReviewResponseDto>> getReviews(
            @PathVariable Long eventId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<ReviewResponseDto> response = reviewService.getReviews(eventId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리뷰 수정", description = "본인 리뷰만 수정 가능")
    @PutMapping("/{reviewId}/events/{eventId}")
    public ResponseEntity<String> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequestDto requestDto,
            @AuthenticationPrincipal String email
    ) {
        reviewService.updateReview(
                reviewId,
                requestDto,
                email
        );
        return ResponseEntity.ok("리뷰가 성공적으로 수정되었습니다.");
    }

    @Operation(summary = "리뷰 삭제", description = "본인 또는 ADMIN만 삭제 가능")
    @DeleteMapping("/{reviewId}/events/{eventId}")
    public ResponseEntity<String> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal String email
    ) {
        reviewService.deleteReview(
                reviewId,
                email
        );
        return ResponseEntity.ok("리뷰가 성공적으로 삭제되었습니다.");
    }
}