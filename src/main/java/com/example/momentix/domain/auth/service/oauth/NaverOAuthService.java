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

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverOAuthService implements OAuthService {
    // 네이버 OAuth에서 Authorization Code → Access Token으로 교환할 때 호출하는 API 주소
    private static final String TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
    private static final String PROFILE_URL = "https://openapi.naver.com/v1/nid/me";


    private final OAuthClient oAuthClient;
    private final UserRepository userRepository;
    private final SignInRepository signInRepository;
    private final ObjectMapper objectMapper;


    @Value("${naver.client.id}")
    private String clientId;
    @Value("${naver.client.secret}")
    private String clientSecret;
    @Value("${naver.redirect.uri}")
    private String redirectUri;

    @Transactional
    public OAuthSignInResponse signIn(String code, String state) {
        try {
            String tokenResponse = oAuthClient.postForm(
                    TOKEN_URL,
                    "grant_type", "authorization_code",
                    "client_id", clientId,
                    "client_secret", clientSecret,
                    "code", code,
                    "state", state,
                    "redirect_uri", redirectUri
            );

            JsonNode tokenJson = objectMapper.readTree(tokenResponse);
            String accessToken = tokenJson.get("access_token").asText("");
            if (accessToken == null) {
                throw new AuthErrorException(OAUTH_TOKEN_EXCHANGE_FAILED);
            }

            String profileResponse = oAuthClient.get(PROFILE_URL, accessToken);
            JsonNode root = objectMapper.readTree(profileResponse);

            if (!"00".equals(root.get("resultcode").asText())) {
                throw new AuthErrorException(OAUTH_PROFILE_FETCH_FAILED);
            }
            JsonNode response = root.get("response");

            String email = response.get("email").asText();
            String nickname = response.get("nickname").asText();
            String name = response.path("name").asText(null);
            String birthyear = response.path("birthyear").asText(null);
            String birthday = response.path("birthday").asText(null);
            String mobile = response.path("mobile").asText(null);
            if (email == null || nickname == null) {
                throw new AuthErrorException(OAUTH_PROFILE_FETCH_FAILED);
            }

            LocalDate birthDate = toBirthDate(birthyear, birthday);
            String phoneNumber = normalizePhone(mobile);

            Users user = userRepository.findBySignIn_Username(email).orElseGet(() -> {
                Users users = new Users();
                users.setNickname(nickname);
                users.setRole(RoleType.CONSUMER);
                users.setBirthDate(birthDate);
                users.setPhoneNumber(phoneNumber);
                return userRepository.save(users);
            });

            signInRepository.findByUsername(email).orElseGet(() -> {
                SignIn signIn = new SignIn();
                signIn.setUsername(email);
                signIn.setPassword("{noop}");
                signIn.setUsers(user);
                return signInRepository.save(signIn);
            });

            String accessJwt = JwtUtil.createAccessToken(user.getUserId(), email, user.getRole());
            String refreshJwt = JwtUtil.createRefreshToken(user.getUserId());

            return new OAuthSignInResponse(
                    accessJwt, refreshJwt, user.getUserId(), email, user.getNickname(), "NAVER"
            );

        } catch (Exception e) {
            throw new AuthErrorException(OAUTH_PROVIDER_ERR);
        }

    }

    private LocalDate toBirthDate(String year, String monthDay) {
        if (year == null || monthDay == null)
            return null;
        String[] md = monthDay.split("-");
        if (md.length != 2)
            return null;
        try {
            return LocalDate.of(
                    Integer.parseInt(year),
                    Integer.parseInt(md[0]),
                    Integer.parseInt(md[1])
            );
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizePhone(String mobile) {
        if (mobile == null)
            return null;
        String digits = mobile.replaceAll("[^0-9]", "");
        if (digits.length() == 11 && digits.startsWith("010")) {
            return digits; 
        }
        return digits;
    }
}

