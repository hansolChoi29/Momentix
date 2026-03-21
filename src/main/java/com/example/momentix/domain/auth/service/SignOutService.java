package com.example.momentix.domain.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignOutService {
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String REFRESH_TOKEN = "REFRESH_TOKEN";

    public void signOut(HttpServletResponse response) {
        ResponseCookie expiredAccess = ResponseCookie.from(ACCESS_TOKEN, "")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredAccess.toString());

        ResponseCookie expiredRefresh = ResponseCookie.from(REFRESH_TOKEN, "")
                .path("/auth/refresh")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredRefresh.toString());
    }
}
