package com.example.momentix.domain.bookmark.service;

import com.example.momentix.domain.bookmark.dto.response.BookmarkResponseDto;
import com.example.momentix.domain.bookmark.entity.Bookmark;
import com.example.momentix.domain.bookmark.repository.BookmarkRepository;
import com.example.momentix.domain.common.exception.auth.AuthErrorCode;
import com.example.momentix.domain.common.exception.auth.AuthErrorException;
import com.example.momentix.domain.common.exception.event.EventErrorException;
import com.example.momentix.domain.events.entity.Events;
import com.example.momentix.domain.events.entity.eventimages.EventImages;
import com.example.momentix.domain.events.repository.EventsRepository;
import com.example.momentix.domain.events.repository.eventimages.EventImagesRepository;
import com.example.momentix.domain.users.entity.Users;
import com.example.momentix.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.momentix.domain.common.exception.event.EventErrorCode.*;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final EventsRepository eventsRepository;
    private final EventImagesRepository eventImagesRepository;
    private final UserRepository userRepository;

    private Users getUser(String email) {
        return userRepository.findBySignIn_Username(email)
                .orElseThrow(() -> new AuthErrorException(AuthErrorCode.NOT_FOUND));
    }

    @Transactional
    public boolean toggleBookmark(Long eventId, String email) {
        Users user = getUser(email);
        Events event = eventsRepository.findById(eventId)
                .orElseThrow(() -> new EventErrorException(NOT_EVENT));

        // 1. 기존에 즐겨찾기 정보가 있는지 조회
        Optional<Bookmark> bookmarkOptional = bookmarkRepository.findByUsersAndEvents(user, event);

        if (bookmarkOptional.isPresent()) {
            // 2. 정보가 있으면, 상태를 반전(toggle)시킵니다.
            Bookmark bookmark = bookmarkOptional.get();
            bookmark.toggleStatus();
            return bookmark.isBookmarkStatus();
        } else {
            // 3. 정보가 없으면, 새로 생성하고 저장합니다.
            Bookmark bookmark = new Bookmark(user, event);
            bookmarkRepository.save(bookmark);
            return true;
        }
    }

    public Page<BookmarkResponseDto> getMyBookmarks(String email, Pageable pageable) {
        Users user = getUser(email);
        Page<Bookmark> bookmarkPage = bookmarkRepository.findByUsersAndBookmarkStatusTrue(user, pageable);

        // Page<Bookmark>를 Page<BookmarkResponseDto>로 변환하는 로직을 수정합니다.
        return bookmarkPage.map(bookmark -> {
            // 1. 북마크에서 Events 객체를 가져옵니다.
            Events event = bookmark.getEvents();

            // 2. Events 객체로 EventImage 정보를 조회합니다. (없을 수도 있으므로 orElse(null) 처리)
            EventImages eventImages = eventImagesRepository.findByEvents(event).orElse(null);

            // 3. Events와 EventImage 정보를 모두 사용하여 DTO를 생성합니다.
            return new BookmarkResponseDto(event, eventImages);
        });
    }
}