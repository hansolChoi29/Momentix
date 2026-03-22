package com.example.momentix.domain.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class QueueConsumer implements StreamListener<String, MapRecord<String, String, String>> {
    private final RedisTemplate<String, String> redisTemplate;
    private final QueueWebSocketHandler webSocketHandler;

    public QueueConsumer(
            RedisTemplate<String, String> redisTemplate,
            QueueWebSocketHandler webSocketHandler
    ) {
        this.redisTemplate = redisTemplate;
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void onMessage(
            MapRecord<String, String, String> message
    ) {
        String eventId = message.getStream() != null ? String.valueOf(message.getStream().split(":")[1]) : null;
        String token = message.getValue().get("token");
        String status = message.getValue().get("status");
        String position = message.getValue().get("position");
        log.info("onMessage 동작");

        String payload = Map.of(
                "token", token,
                "status", status
        ).toString();

        if ("ALLOWED".equals(status)) {
            String tokenInfo = redisTemplate.opsForValue().get("token:" + eventId + ":" + token);
            if (tokenInfo == null || !tokenInfo.contains(":")) {
                log.warn("QueueConsumer: token 매핑 정보 없음. eventId={}, token={}", eventId, token);
                return;
            }

            String sessionId = tokenInfo.split(":")[0];
            String userId = tokenInfo.split(":")[1];

            if (sessionId != null && userId != null) {
                try {
                    webSocketHandler.sendMessage(userId, "userId : " + userId + "예매 가능 상태입니다. " + payload);
                    redisTemplate.opsForStream().acknowledge(message.getStream(), "eventQueueGroup" + eventId, message.getId());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        log.info("token: {} status: {} position: {}", token, status, position);
    }
}
