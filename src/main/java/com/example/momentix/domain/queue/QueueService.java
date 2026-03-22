package com.example.momentix.domain.queue;


import com.example.momentix.domain.common.exception.auth.AuthErrorCode;
import com.example.momentix.domain.common.exception.auth.AuthErrorException;
import com.example.momentix.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {
    private final RedisTemplate<String, String> redisTemplate;
    private final QueueRegisterStreamService queueRegisterStreamService;
    private final UserRepository userRepository;
    private final String allowKey = "allow:";
    private final String userToTokenKey = "user";
    private final String sessionToTokenKey = "session";
    // key값 생성 공연마다 대기열 구분 토큰으로 관리
    private final String eventQueueKey = "queue:";
    // key값 생성 토큰 -> 세션 ID (공연별 알림 발송용, eventId로 구분)
    private final String tokenKey = "token:";
    private final String streamKey = "stream:";
    // UUID 값 토큰으로 관리

    private Long getUserId(String email) {
        return userRepository.findBySignIn_Username(email)
                .orElseThrow(() -> new AuthErrorException(AuthErrorCode.NOT_FOUND))
                .getUserId();
    }

    /**
     * Allowed 상태 유저 수 count
     *
     * @param eventId 공연마다 Allowed 상태 유저 수
     * @return Allowed 상태 유저 수 반환 default : 0
     */
    public Long countAllow(Long eventId) {
        String count = redisTemplate.opsForValue().get(allowKey + eventId);
        if (count == null) {
            return 0L;
        }
        return Long.parseLong(count);
    }

    public String getToken(Long userId, Long eventId) {
        return redisTemplate.opsForValue().get(userToTokenKey + eventId + ":" + userId);
    }

    /**
     * 예매중인 공연 redis에 저장으로 관리
     *
     * @param eventId 예매중인 공연 리스트
     */
    public void eventList(Long eventId) {
        String activeEventKey = "activeEvent";
        boolean isActive = !Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(activeEventKey, String.valueOf(eventId)));
        if (isActive) {
            redisTemplate.opsForSet().add(activeEventKey, String.valueOf(eventId));
        }

    }

    /**
     * 예매하기 버튼 클릭시
     * 대기열 등록
     *
     * @param email     유저 ID
     * @param sessionId 유저 세션 ID
     * @param eventId   공연 ID
     */
    public String addQueue(String email, String sessionId, Long eventId) {
        Long userId = getUserId(email);
        // 기존에 이 공연 대기열에 등록된 유저(Id)면 등록 안함
        eventList(eventId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(userToTokenKey + eventId + ":" + userId))) {
            throw new IllegalIdentifierException("등록된 유저");
        }
        if (Boolean.TRUE.equals(redisTemplate.hasKey(sessionToTokenKey + eventId + ":" + sessionId))) {
            throw new IllegalIdentifierException("등록된 기기");
        }
        String token = UUID.randomUUID().toString();
        // timestamp로 FIFO 구현 + random 값으로 동시에 요청시 우선순위 나누기
        double score = System.currentTimeMillis() + Math.random();

        // token 대기열 배정
        redisTemplate.opsForZSet().add(eventQueueKey + eventId, token, score);

        String tokenInfo = sessionId + ":" + userId;
        redisTemplate.opsForValue().set(tokenKey + eventId + ":" + token, tokenInfo, 30, TimeUnit.MINUTES);
        // sessionId -> token 매핑 중복유저 방지용 + userId -> token 매핑 중복유저 방지용
        redisTemplate.opsForValue().set(sessionToTokenKey + eventId + ":" + sessionId, token, 30, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(userToTokenKey + eventId + ":" + userId, token, 30, TimeUnit.MINUTES);

        return token;
    }

    @Async
    public void processQueue(Long eventId) {
        int batchSize = 3;

        int allowSize = countAllow(eventId).intValue();
        if (batchSize - allowSize > 0) {
            Set<String> batch = redisTemplate.opsForZSet().range(eventQueueKey + eventId, 0, batchSize - allowSize - 1);
            if (batch == null || batch.isEmpty()) {
                return;
            }
            for (String token : batch) {
                String tokenInfo = redisTemplate.opsForValue().get(tokenKey + eventId + ":" + token);
                if (tokenInfo == null || !tokenInfo.contains(":")) {
                    // 이 token의 매핑 정보가 없음 (TTL 만료 or 이미 처리됨) → 대기열에서 제거 후 skip
                    log.warn("processQueue: token 매핑 없음, 대기열에서 제거. eventId={}, token={}", eventId, token);
                    redisTemplate.opsForZSet().remove(eventQueueKey + eventId, token);
                    continue;
                }

                String sessionId = tokenInfo.split(":")[0];
                String userId = tokenInfo.split(":")[1];

                if (sessionId == null || userId == null) {
                    continue;
                }

                String status = "ALLOWED";
                Map<String, String> msg = Map.of(
                        "token", token,
                        "status", status
                );
                RecordId streamId = redisTemplate.opsForStream().add(streamKey + eventId, msg);
                if (streamId != null) {
                    redisTemplate.opsForValue().set(token, streamId.getValue(), 10, TimeUnit.MINUTES);
                    redisTemplate.opsForValue().increment(allowKey + eventId);
                }

                redisTemplate.opsForZSet().remove(eventQueueKey + eventId, token);
                redisTemplate.delete(List.of(
                        userToTokenKey + eventId + ":" + userId,
                        sessionToTokenKey + eventId + ":" + sessionId
                ));
                queueRegisterStreamService.registerStream(eventId);
            }
        } else {
            Set<String> waiting = redisTemplate.opsForZSet().range(eventQueueKey + eventId, 0, -1);
            if (waiting != null && !waiting.isEmpty()) {
                for (String token : waiting) {
                    rankAlarmQueue(eventId, token);
                }
            }
        }
    }

    public void rankAlarmQueue(Long eventId, String token) {
        String streamRankKey = "streamRank:" + eventId;
        Long position = redisTemplate.opsForZSet().rank(eventQueueKey + eventId, token);
        if (position == null) {
            return;
        }
        String status = "WAITING";
        Map<String, String> msg = Map.of(
                "token", token,
                "status", status,
                "position", String.valueOf(position + 1)
        );
        redisTemplate.opsForStream().add(streamRankKey, msg);
        redisTemplate.expire(streamRankKey, 30, TimeUnit.MINUTES);
        queueRegisterStreamService.alarmStream(eventId);
    }


    public void completeQueue(Long eventId, String token) {
        String streamId = redisTemplate.opsForValue().get(token);

        if (streamId != null) {
            redisTemplate.opsForStream().delete(streamKey + eventId, streamId);
            redisTemplate.delete(tokenKey + eventId + ":" + token);
            redisTemplate.delete(token);
            redisTemplate.opsForValue().decrement(allowKey + eventId);
        }
        processQueue(eventId);
    }
}
