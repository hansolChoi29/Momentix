package com.example.momentix.domain.auth.dto;

public class SignInDto {
    private final String accessToken;
    private final String refreshToken;

    public SignInDto(String accessToken, String refreshToken) {
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
