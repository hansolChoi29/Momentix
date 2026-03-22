package com.example.momentix.domain.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class RankConsumer implements StreamListener<String, MapRecord<String, String, String>> {
    private final RedisTemplate<String, String> redisTemplate;
    private final QueueWebSocketHandler webSocketHandler;

    public RankConsumer(RedisTemplate<String, String> redisTemplate, QueueWebSocketHandler webSocketHandler) {
        this.redisTemplate = redisTemplate;
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        String eventId = message.getStream() != null ? String.valueOf(message.getStream().split(":")[1]) : null;
        String token = message.getValue().get("token");
        String status = message.getValue().get("status");
        String position = message.getValue().get("position");
        log.info("onMessage 동작");
        String payload = Map.of(
                "token", token,
                "status", status
        ).toString();
        if ("WAITING".equals(status)) {
            /* TODO : .get()이 null을 반환하면 바로 NullPointerException 터짐
             * TTL 만료 시 processQueue에서 이미 key를 삭제한 후에 consumer 쓰레드가 이 줄 실행하면
             * 앱 전체가 RuntimeException으로 뻗을 위험
             * 스트림 리스너 다이 - 대기열 전체 멈춤
             */
            String sessionId = redisTemplate.opsForValue().get("token:" + eventId + ":" + token).split(":")[0];
            String userId = redisTemplate.opsForValue().get("token:" + eventId + ":" + token).split(":")[1];

            if (sessionId != null && userId != null) {
                try {
                    webSocketHandler.sendMessage(userId, "userId : " + userId + "현재 순위는 : " + position + "입니다." + payload);
                    redisTemplate.opsForStream().acknowledge(message.getStream(), "alarmQueueGroup" + eventId, message.getId());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                log.info(sessionId, position, payload);
            }
        }
        log.info("token: {} status: {} position: {}", token, status, position);
    }
}
