package com.example.momentix.domain.auth.dto;

public class SignInResponse {
    private final String accessToken;
    private final String refreshToken;

    public SignInResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
