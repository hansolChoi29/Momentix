package com.example.momentix.domain.search.bootstrap;


import com.example.momentix.domain.search.bootstrap.service.EventIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnProperty(name = "search.bootstrap.enabled", havingValue = "true")// 로컬만 켜두고 기본(배표)은 꺼두기
public class EventIndexBootstrapper {
    // 실행조건: 설정(search.bootstrap.enabled=true)일 때만 동작
    // ES 인덱스(테이블 같은 것) 없으면 만들고
    // MySQL에 있는 이벤트+장소 데이터를 전부 ES에 넣는다

    private static final Logger log = LoggerFactory.getLogger(EventIndexBootstrapper.class);

    private final EventIndexService eventIndexService;

    public EventIndexBootstrapper(EventIndexService eventIndexService) {
        this.eventIndexService = eventIndexService;
    }

    //스프링 부트가 켜질 때 자동으로 한 번 실행됨
    @Bean
    public ApplicationRunner initEventIndex() {
        return args -> {
            try {
                // 1) ES에 events 인덱스(테이블 같은 것) 없으면 생성
                eventIndexService.ensureEventsIndex();
                // 1) ES에 events 인덱스(테이블 같은 것) 없으면 생성
                eventIndexService.reindexAllFromMySQL();
                log.info("[ES] Bootstrap finished");
            } catch (Exception e) {
                // 실패해도 서버 전체는 계속 올라가도록 로그만 남김
                log.error("[ES] Bootstrap failed", e);
            }
        };
    }
}
