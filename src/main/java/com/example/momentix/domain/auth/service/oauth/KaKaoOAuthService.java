package com.example.momentix.domain.auth.service.oauth;

import com.example.momentix.domain.auth.dto.response.OAuthSignInResponse;
import com.example.momentix.domain.auth.entity.RoleType;
import com.example.momentix.domain.auth.entity.SignIn;
import com.example.momentix.domain.auth.repository.SignInRepository;
import com.example.momentix.domain.auth.util.OAuthClient;
import com.example.momentix.domain.common.util.JwtUtil;
import com.example.momentix.domain.users.entity.Users;
import com.example.momentix.domain.users.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.momentix.domain.common.exception.auth.AuthErrorException;
import static com.example.momentix.domain.common.exception.auth.AuthErrorCode.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class KaKaoOAuthService implements OAuthService {

    //code를 주고 access_token / refresh_token을 교환받는 URL.
    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String PROFILE_URL = "https://kapi.kakao.com/v2/user/me";

    private final OAuthClient oAuthClient;
    private final UserRepository userRepository;
    private final SignInRepository signInRepository;
    private final ObjectMapper objectMapper;

    @Value("${kakao.client.id}")
    private String clientId;
    @Value("${kakao.client.secret}")
    private String clientSecret;
    @Value("${kakao.redirect.uri}")
    private String redirectUri;

    @Transactional
    @Override
    public OAuthSignInResponse signIn(String code, String state) {
        try {
            // 1) 토큰 요청
            String tokenResponse = oAuthClient.postForm(
                    TOKEN_URL,
                    "grant_type", "authorization_code",// 토큰 발급 방식
                    "client_id", clientId,// = REST API 키
                    "code", code,// 로그인 성공 티켓(임시 번호=인가코드)
                    "redirect_uri", redirectUri,
                    "client_secret", clientSecret
            );

            //소셜 토큰 발급
            JsonNode tokenJson = objectMapper.readTree(tokenResponse);
            String accessToken = tokenJson.get("access_token").asText(null);
            if (accessToken == null) {
                throw new AuthErrorException(OAUTH_TOKEN_EXCHANGE_FAILED);
            }

            // 2) 프로필 조회
            String profileResponse = oAuthClient.get(PROFILE_URL, accessToken);
            JsonNode root = objectMapper.readTree(profileResponse);
            JsonNode kakaoAccount = root.path("kakao_account");

            String email = kakaoAccount.path("email").asText(null);
            String nickname = kakaoAccount.path("profile").path("nickname").asText(null);
            String profileImage = kakaoAccount.path("profile").path("profile_image_url").asText(null);

            if (email == null) {
                throw new AuthErrorException(OAUTH_EMAIL_SCOPE_REQUIRED);
            }

            // 3) 유저 조회/생성
            Users user = userRepository.findBySignIn_Username(email).orElseGet(() -> {
                Users newUser = new Users();
                newUser.setNickname(nickname);
                newUser.setRole(RoleType.CONSUMER);
                return userRepository.save(newUser);
            });

            signInRepository.findByUsername(email).orElseGet(() -> {
                SignIn signIn = new SignIn();
                signIn.setUsername(email);
                signIn.setPassword("{noop}");
                signIn.setUsers(user);
                return signInRepository.save(signIn);
            });

            // 4) JWT 발급
            String accessJwt = JwtUtil.createAccessToken(user.getUserId(), email, user.getRole());
            String refreshJwt = JwtUtil.createRefreshToken(user.getUserId());

            return new OAuthSignInResponse(
                    accessJwt, refreshJwt, user.getUserId(), email, user.getNickname(), "KAKAO"
            );

        } catch (Exception e) {
            throw new AuthErrorException(OAUTH_PROVIDER_ERROR);
        }
    }
}