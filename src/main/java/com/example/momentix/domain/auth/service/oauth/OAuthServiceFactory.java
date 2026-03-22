package com.example.momentix.domain.auth.service.oauth;

import com.example.momentix.domain.auth.entity.OAuthProvider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static com.example.momentix.domain.common.exception.auth.AuthErrorCode.*;
import com.example.momentix.domain.common.exception.auth.AuthErrorException;


@Component
@RequiredArgsConstructor
public class OAuthServiceFactory {
    private final NaverOAuthService naverOAuthService;
    private final KaKaoOAuthService kaKaoOAuthService;

    public OAuthService getOAuthService(OAuthProvider provider) {
        switch (provider) {
            case NAVER:
                return naverOAuthService;
            case KAKAO:
                return kaKaoOAuthService;
            default:
                throw new AuthErrorException(UNSUPPORTED_OAUTH_PROVIDER);
        }
    }
}
