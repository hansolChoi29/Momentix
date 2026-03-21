package com.example.momentix.domain.bookmark.controller;

import com.example.momentix.domain.bookmark.dto.response.BookmarkResponseDto;
import com.example.momentix.domain.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/users/favorites/events/{eventId}")
    public ResponseEntity<?> toggleBookmark(
            @PathVariable Long eventId,
            @AuthenticationPrincipal String email
    ) {
        boolean resultStatus = bookmarkService.toggleBookmark(
                eventId,
                email
        );
        Map<String, Boolean> response = Map.of("bookmarkStatus", resultStatus);

        return new ResponseEntity<>(
                response,
                HttpStatus.OK
        );
    }

    @GetMapping("/users/favorites")
    public ResponseEntity<Page<BookmarkResponseDto>> getMyBookmarks(
            @AuthenticationPrincipal String email,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<BookmarkResponseDto> response = bookmarkService.getMyBookmarks(
                email,
                pageable
        );
        return ResponseEntity.ok(response);
    }
}