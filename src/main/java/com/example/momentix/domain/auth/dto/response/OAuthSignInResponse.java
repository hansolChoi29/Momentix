package com.example.momentix.domain.auth.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OAuthSignInResponse {
    private final String accessToken;
    private final String refreshToken;
    private final Long userId;
    private final String email;
    private final String nickname;
    private final String provider; //카카로 네이버
}
