package com.example.momentix.domain.review.controller;

import com.example.momentix.domain.review.dto.request.CreateReviewRequestDto;
import com.example.momentix.domain.review.dto.request.UpdateReviewRequestDto;
import com.example.momentix.domain.review.dto.response.ReviewResponseDto;
import com.example.momentix.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/events/{eventId}")
    public ResponseEntity<ReviewResponseDto> createReview(
            @PathVariable Long eventId,
            @RequestBody CreateReviewRequestDto requestDto,
            @AuthenticationPrincipal String email
    ) {
        ReviewResponseDto response = reviewService.createReview(
                eventId,
                requestDto,
                email
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<Page<ReviewResponseDto>> getReviews(
            @PathVariable Long eventId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<ReviewResponseDto> response = reviewService.getReviews(eventId, pageable);
        return ResponseEntity.ok(response);
    }

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