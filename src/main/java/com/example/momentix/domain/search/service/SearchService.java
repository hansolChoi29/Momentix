package com.example.momentix.domain.search.service;

import com.example.momentix.domain.common.elasticsearch.IndexNames;
import com.example.momentix.domain.events.repository.EventsRepository;
import com.example.momentix.domain.search.dto.*;
import com.example.momentix.domain.search.repository.AnalyticsRepository;
import com.example.momentix.domain.search.repository.SuggestRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class SearchService {
    private static final Logger log = LoggerFactory.getLogger(SearchService.class);
    private final EventsRepository eventsRepository;
    private final SuggestRepository suggestRepository;
    private final AnalyticsRepository analyticsRepository;
    // 인기 검색어 캐시 (1시간 단위 랭킹 고정 노출)
    private volatile List<AutocompleteResponse> cachedPopular = null;
    private volatile long popularCacheExpireAtMillis = 0L; // 캐시 만료 시각(밀리초)

    @Transactional(readOnly = true)
    public Page<SearchResponseDto> searchEvent(
            SearchRequestDto searchRequestdto,
            Pageable pageable
    ) {
        return eventsRepository.searchEventByParam(
                searchRequestdto,
                pageable
        );
    }
    //엘라스틱서치

    // 자동완성
    @Transactional(readOnly = true)
    public List<AutocompleteResponse> autocomplete(
            String input,
            int size
    ) {
        int limit = size > 0 ? size : IndexNames.DEFAULT_SUGGEST_SIZE;
        List<AutocompleteResponse> result = suggestRepository.suggest(input, limit);
        // 결과 없으면 인기검색어로 폴백
        if (result.isEmpty()) {
            return popularQueries(1, limit);
        }
        return result;
    }

    // 1-1. 시간대별 검색 수 집계
    // 스케줄러용 (1시간 단위, void)
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    public void hourlyCountsScheduler() {
        int hours = 1;
        List<HourlyCountBucket> result = analyticsRepository.countPerHour(hours);
        log.info("최근 1시간 검색 집계: {}", result);
    }

    // 1-2. 시간대별 검색 수 집계
    // 컨트롤러용 (사용자 요청에 따라 hours 조정 가능)
    @Transactional(readOnly = true)
    public List<HourlyCountBucket> getHourlyCounts(int hours) {
        return analyticsRepository.countPerHour(hours);
    }

    @Transactional(readOnly = true)
    public List<AutocompleteResponse> popularQueries(
            int hours,
            int size
    ) {
        final int topN = (size > 0) ? size : 10;
        final long now = System.currentTimeMillis();

        // 1) 캐시 유효하면 그대로 반환 (ES 재조회 막기)
        List<AutocompleteResponse> local = cachedPopular; // volatile 읽기
        if (local != null && now < popularCacheExpireAtMillis) {
            return (local.size() > topN) ? local.subList(0, topN) : local;
        }

        // 2) 캐시 만료/미존재 → 동기화 블록에서 한 번만 갱신
        synchronized (this) {
            // 들어오는 동안 이미 갱신됐을 수 있으니 재확인
            if (cachedPopular != null && System.currentTimeMillis() < popularCacheExpireAtMillis) {
                return (cachedPopular.size() > topN) ? cachedPopular.subList(0, topN) : cachedPopular;
            }

            // 기획 고정: 최근 1시간 집계만 사용
            List<AutocompleteResponse> fresh = analyticsRepository.popularQueries(1, topN);

            // 캐시에 저장 + 만료시각을 다음 정각으로 설정 (랭킹 1시간 단위 고정 노출)
            cachedPopular = (fresh == null) ? List.of() : fresh;
            popularCacheExpireAtMillis = nextTopOfHourMillis();

            return cachedPopular;
        }
    }

    // 검색 로그 비동기 적재 (인기검색어 집계의 원천 데이터)
    @Async
    public void logSearchAsync(
            SearchRequestDto request,
            String userId,
            String ip
    ) {
        if (request == null || request.getQuery() == null || request.getQuery().isBlank()) return;

        String category = (request.getEventCategory() == null) ? null : request.getEventCategory().name();
        String start = (request.getSearchStartDate() == null) ? null : request.getSearchStartDate().toString();
        String end = (request.getSearchEndDate() == null) ? null : request.getSearchEndDate().toString();

        SearchLogDoc doc = new SearchLogDoc(
                request.getQuery(),
                category,
                start,
                end,
                userId,
                ip,
                Instant.now().toEpochMilli()
        );
        analyticsRepository.indexSearchLog(doc);
    }

    // 다음 정각(Asia/Seoul)까지 → 랭킹을 딱 1시간 간격으로 갱신
    private long nextTopOfHourMillis() {
        ZonedDateTime nextHour = ZonedDateTime.now(ZoneId.of(IndexNames.TIME_ZONE_SEOUL))
                .withMinute(0).withSecond(0).withNano(0)
                .plusHours(1);
        return nextHour.toInstant().toEpochMilli();
    }
}