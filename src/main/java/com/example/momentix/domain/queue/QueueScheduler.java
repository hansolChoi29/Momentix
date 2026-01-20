package com.example.momentix.domain.queue;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@RequiredArgsConstructor
@Component
@Profile("prod") // 리펙토링 단계, 운영환경으로 돌려 놓음
public class QueueScheduler {

    private final QueueService queueService;
    private final RedisTemplate<String, String> redisTemplate;
    /**
     * 1초마다 백그라운드 워커에서 실행
     * 자동으로 예매 가능 상태로 전환 (BatchSize 만큼 여유 잇을 때)
     */
    @Scheduled(fixedRate = 2000)
    @Async("queueTask")
    public void processQueue() {

        Set<String> eventIds = redisTemplate.opsForSet().members("activeEvent");

        if (eventIds != null) {
            for (String eventId : eventIds) {
                Long activeEventId = Long.parseLong(eventId);

                queueService.processQueue(activeEventId);

            }
        }
    }
}
