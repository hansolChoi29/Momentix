package com.example.momentix.domain.search.controller;

import com.example.momentix.domain.search.dto.AutocompleteResponse;
import com.example.momentix.domain.search.dto.HourlyCountBucket;
import com.example.momentix.domain.search.dto.SearchRequestDto;
import com.example.momentix.domain.search.dto.SearchResponseDto;
import com.example.momentix.domain.search.service.SearchService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<Page<SearchResponseDto>> searchEvent(
            @ModelAttribute SearchRequestDto searchRequestDto,
            Pageable pageable,
            HttpServletRequest request, // 클라이언트 요청 정보 (IP 등)
            @AuthenticationPrincipal String email
    ) {
        Page<SearchResponseDto> response = searchService.searchEvent(searchRequestDto, pageable);

        // 검색 로그를 저장(검색어, 카테고리, 기간, 사용자, IP 등)
        /// 검색 결과를 반호나하는 것과 동시에 (비동기)
        // 뒤에서 몰래 기록 작업이 처리 됨(사용자가 기다릴 필요 없음)

        searchService.logSearchAsync(
                searchRequestDto,
                email,
                extractClientIp(request)
        );

        return ResponseEntity.ok(response);
    }

    // 사용자의 진짜 IP주소 가져오기
    // 중간 서버를 거칠 수 있어서 헤더 값에서 먼저 꺼내보고 없으면 기본 IP 사용
    private String extractClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank())
            return xff.split(",")[0].trim();
        String realIp = req.getHeader("X-Real-IP");
        return (realIp != null && !realIp.isBlank()) ? realIp : req.getRemoteAddr();
    }

    // 자동완성- 그 글자로 시작하는 추천 단어
    @GetMapping("/autocomplete")
    public List<AutocompleteResponse> autocomplete(
            @RequestParam("q") String query,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return searchService.autocomplete(query, size);
    }

    // 인기검색어
    @GetMapping("/popular-queries")
    public List<AutocompleteResponse> popularQueries(
            @RequestParam(value = "hours", defaultValue = "1") int hours,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return searchService.popularQueries(hours, size);
    }

    // 시간대별 건수
    @GetMapping("/hourly-counts")
    public List<HourlyCountBucket> hourlyCounts(
            @RequestParam(value = "hours", defaultValue = "1") int hours) {
        return searchService.getHourlyCounts(hours);
    }
}
