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
import com.example.momentix.domain.users.repository.UserRepository;
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
    private final TicketRepository ticketRepository;
    private final ReviewRepository reviewRepository;
    private final EventsRepository eventsRepository;
    private final UserRepository userRepository;

    private Users getUser(String email) {
        return userRepository.findBySignIn_Username(email)
                .orElseThrow(() -> new ReviewErrorException(FORBIDDEN));
    }

    @Transactional
    public ReviewResponseDto createReview(
            Long eventId,
            CreateReviewRequestDto requestDto,
            String email
    ) {
        Users users = getUser(email);
        validateRating(requestDto.getRating());

        Events event = eventsRepository.findById(eventId)
                .orElseThrow(() -> new EventErrorException(EVENT_NOT_FOUND));

        boolean hasPurchased = ticketRepository.existsByUserIdAndEventId(
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
            String email
    ) {
        Users users = getUser(email);
        validateRating(requestDto.getRating());

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewErrorException(REVIEW_NOT_FOUND));

        if (!review.getUsers().getUserId().equals(users.getUserId())) {
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
            String email
    ) {
        Users users = getUser(email);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewErrorException(REVIEW_NOT_FOUND));
        boolean isAdmin = users.getRole() == RoleType.ADMIN;
        boolean isOwner = review.getUsers().getUserId().equals(users.getUserId());

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