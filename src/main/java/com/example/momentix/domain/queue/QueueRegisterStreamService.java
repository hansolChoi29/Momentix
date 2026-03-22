package com.example.momentix.domain.queue;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueRegisterStreamService {
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;
    private final QueueConsumer queueConsumer;
    private final RankConsumer rankConsumer;
    private final Map<Long, Boolean> registeredStreams = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> alarmStreams = new ConcurrentHashMap<>();
    private final RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    public void startContainer() {
        if (!container.isRunning()) {
            container.start();
        }
    }

    public void registerStream(Long eventId) {
        String streamKey = "stream:" + eventId;

        /* TODO : key 타입 불일치
         * String으로 조회하는데 Long으로 저장
         *
         * map의 제네릭은 Map<Long, Boolean>인데 containsKey(streamKey)에서 String임
         * Long, String 불일치
         * */

        if (registeredStreams.containsKey(streamKey)) {
            return;
        }
        try {
            redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0"), "eventQueueGroup" + eventId);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("BUSY GROUP")) {
                throw new IllegalIdentifierException("이미 존재하는 그룹");
            } else {
                return;
            }
        }

        container.receive(
                Consumer.from("eventQueueGroup" + eventId, "consumer" + eventId),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                queueConsumer
        );
        log.info("Stream 등록 확인");
        registeredStreams.put(eventId, true);

    }

    public void alarmStream(Long eventId) {
        String streamRankKey = "streamRank:" + eventId;
        // TODO : 여기도 타입 불일치 String
        if (alarmStreams.containsKey(streamRankKey)) {
            return;
        }

        try {
            redisTemplate.opsForStream().createGroup(streamRankKey, ReadOffset.from("0"), "alarmQueueGroup" + eventId);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("BUSY GROUP")) {
                throw new IllegalIdentifierException("이미 존재하는 그룹");
            } else {
                return;
            }
        }

        container.receive(
                Consumer.from("alarmQueueGroup" + eventId, "consumer" + eventId),
                StreamOffset.create(streamRankKey, ReadOffset.lastConsumed()),
                rankConsumer
        );
        alarmStreams.put(eventId, true);

    }
}
