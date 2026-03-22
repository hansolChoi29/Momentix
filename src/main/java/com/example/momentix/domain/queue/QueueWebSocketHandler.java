package com.example.momentix.domain.queue;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class QueueWebSocketHandler extends TextWebSocketHandler {
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String httpSessionId = (String) session.getAttributes().get("HTTP_SESSION_ID");

        String query = session.getUri().getQuery();
        String userId = null;
        if (query != null && query.contains("userId=")) {
            userId = query.split("userId=")[1];
        }
        if (userId != null) {
            WebSocketSession oldSession = sessions.get(userId);
            if (oldSession != null && oldSession.isOpen()) {
                oldSession.close(CloseStatus.NORMAL.withReason("중복 로그인"));
            }
            sessions.put(userId, session);
        } else if (httpSessionId != null) {
            sessions.put(httpSessionId, session);
        } else{
            sessions.put(session.getId(), session);
        }
        log.info("새로운 웹소켓 연결 : {}, userId = {}", session.getId(), userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String httpSessionId = (String) session.getAttributes().get("HTTP_SESSION_ID");
        String payload = message.getPayload();
        log.info("메시지 수신 : {}", payload);

        for (WebSocketSession webSocketSession : sessions.values()) {
            if (webSocketSession != null && webSocketSession.isOpen()) {
                try {
                    webSocketSession.sendMessage(new TextMessage("서버에서 메시지 보냄 : " + payload));
                    webSocketSession.sendMessage(new TextMessage("서버 : " + httpSessionId));
                } catch (IOException e) {
                    log.error("메시지 전송 실패: {}", e.getMessage());
                }
            }
        }

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String httpSessionId = (String) session.getAttributes().get("HTTP_SESSION_ID");
        if (httpSessionId != null) {
            sessions.remove(httpSessionId);
        }

        String query = session.getUri() != null ? session.getUri().getQuery() : null;

        if(query != null && query.contains("userId=")) {
            sessions.remove(session.getUri().getQuery().split("userId=")[1]);
        }
    }

    public void sendMessage(String userId, String message) throws IOException {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }

    }
}
