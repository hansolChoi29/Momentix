package com.example.momentix.domain.review.service;

import com.example.momentix.domain.auth.entity.RoleType;
import com.example.momentix.domain.common.exception.event.EventErrorException;
import com.example.momentix.domain.common.exception.review.ReviewErrorException;
import com.example.momentix.domain.events.entity.Events;
import com.example.momentix.domain.events.repository.EventsRepository;
import com.example.momentix.domain.review.dto.request.CreateReviewRequestDto;
import com.example.momentix.domain.review.dto.request.UpdateReviewRequestDto;
import com.example.momentix.domain.review.dto.response.ReviewResponseDto;
import com.example.momentix.domain.review.entity.Review;
import com.example.momentix.domain.review.repository.ReviewRepository;
import com.example.momentix.domain.ticket.repository.TicketRepository;
import com.example.momentix.domain.users.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.momentix.domain.common.exception.event.EventErrorCode.EVENT_NOT_FOUND;
import static com.example.momentix.domain.common.exception.review.ReviewCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {
    // TODO : 예매 안 한 사람도 리뷰 작성 가능한 문제 - 해당 유저가 해당 공연 티켓을 가지고 있는지 확인
    private final TicketRepository ticketRepository;
    private final ReviewRepository reviewRepository;
    private final EventsRepository eventsRepository;

    @Transactional
    public ReviewResponseDto createReview(
            Long eventId,
            CreateReviewRequestDto requestDto,
            Users users
    ) {
        validateRating(requestDto.getRating());

        Events event = eventsRepository.findById(eventId)
                .orElseThrow(() -> new EventErrorException(EVENT_NOT_FOUND));

        // TODO: 사용자가 해당 공연을 예매했는지 권한 검증 로직 추가 필요
        boolean hasPurchased = ticketRepository.existsByUsers_UserIdAndEvents_Id(
                users.getUserId(), eventId
        );

        if (!hasPurchased) {
            throw new ReviewErrorException(REVIEW_NOT_PURCHASED);
        }

        Review review = new Review(
                event,
                users,
                requestDto.getContents(),
                requestDto.getRating()
        );

        Review savedReview = reviewRepository.save(review);

        return new ReviewResponseDto(savedReview);
    }

    public Page<ReviewResponseDto> getReviews(
            Long eventId,
            Pageable pageable
    ) {
        Page<Review> reviewPage = reviewRepository.findByEvents_IdAndIsDeletedFalse(eventId, pageable);

        return reviewPage.map(ReviewResponseDto::new);
    }

    @Transactional
    public void updateReview(
            Long reviewId,
            UpdateReviewRequestDto requestDto,
            Users user
    ) {
        validateRating(requestDto.getRating());

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewErrorException(REVIEW_NOT_FOUND));

        if (!review.getUsers().getUserId().equals(user.getUserId())) {
            throw new ReviewErrorException(FORBIDDEN);
        }
        review.update(
                requestDto.getContents(),
                requestDto.getRating()
        );
    }

    @Transactional
    public void deleteReview(
            Long reviewId,
            Users user
    ) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewErrorException(REVIEW_NOT_FOUND));
        // TODO : 본인 or ADMIN만 삭제 가능
        boolean isAdmin = user.getRole() == RoleType.ADMIN;
        boolean isOwner = review.getUsers().getUserId().equals(user.getUserId());

        if (!isAdmin && !isOwner) {
            throw new ReviewErrorException(REVIEW_NOT_AUTHORIZED);
        }

        review.softDelete(); // Review 엔티티에 softDelete() 메소드 추가 필요
    }

    private void validateRating(Double rating) {
        // rating을 0.5로 나눈 나머지가 0이 아니면 (즉, 0.5 단위가 아니면)
        if (rating % 0.5 != 0) {
            throw new ReviewErrorException(INVALID_RATING_UNIT);
        }
    }
}