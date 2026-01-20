package com.example.momentix.domain.auth.service.oauth;

import com.example.momentix.domain.auth.dto.response.OAuthSignInResponse;

public interface OAuthService {
    OAuthSignInResponse signIn(String code, String state);
}
