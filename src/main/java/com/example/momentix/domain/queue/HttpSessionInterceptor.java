package com.example.momentix.domain.queue;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class HttpSessionInterceptor implements HandshakeInterceptor {
    // 제외대상임
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                    ServerHttpResponse response,
                                    WebSocketHandler wsHandler,
                                    Map<String, Object> attributes) throws Exception{
        if (request instanceof ServletServerHttpRequest servletRequest){
            HttpSession session = servletRequest.getServletRequest().getSession();
            attributes.put("HTTP_SESSION_ID", session.getId());
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {}
}
