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

    // token 발급 요청 → 응답(JSON) → Access Token 추출
    @Transactional
    public OAuthSignInResponse signIn(String code, String state) {
        //네이버 인증 서버에 code보내고 토큰 받아오는 단계
        try {
            String tokenResponse = oAuthClient.postForm(
                    TOKEN_URL, // 네이버 토큰 발급 API 주소
                    "grant_type", "authorization_code",// OAuth2 표준: "authorization_code" 방식


                    "client_id", clientId,// 네이버 앱 Client ID
                    "client_secret", clientSecret, // 네이버 앱 Client Secret
                    "code", code,  // 네이버가 redirect_uri로 넘겨준 인증 코드
                    "state", state,   // 요청 때 보냈던 state (CSRF 방지)
                    "redirect_uri", redirectUri
            );

            //Access Token 발급
            JsonNode tokenJson = objectMapper.readTree(tokenResponse);
            String accessToken = tokenJson.get("access_token").asText("");
            if (accessToken == null) {
                throw new AuthErrorException(OAUTH_TOKEN_EXCHANGE_FAILED);
            }

            String profileResponse = oAuthClient.get(PROFILE_URL, accessToken);
            JsonNode root = objectMapper.readTree(profileResponse);

            //Access Token으로 사용자 프로필 조회
            //ex){ "resultcode": "00", "message": "success" }
            if (!"00".equals(root.get("resultcode").asText())) {
                throw new AuthErrorException(OAUTH_PROFILE_FETCH_FAILED);
            }
            JsonNode response = root.get("response");


            // get: 필드가 없으면 NPE
            // paht: 필드 없으면 ""
            String email = response.get("email").asText();
            String nickname = response.get("nickname").asText();
            String name = response.path("name").asText(null);
            String birthyear = response.path("birthyear").asText(null);
            String birthday = response.path("birthday").asText(null);
            String mobile = response.path("mobile").asText(null);
            if (email == null || nickname == null) {
                throw new AuthErrorException(OAUTH_PROFILE_FETCH_FAILED);
            }

            LocalDate birthDate = toBirthDate(birthyear, birthday); // null 허용
            String phoneNumber = normalizePhone(mobile);           // null 또는 "01012345678"

            //유저 DB 조회 후 없으면 생성
            Users user = userRepository.findBySignIn_Username(email).orElseGet(() -> {
                Users users = new Users();
                users.setNickname(nickname);
                users.setRole(RoleType.CONSUMER);
                users.setBirthDate(birthDate);
                users.setPhoneNumber(phoneNumber);
                return userRepository.save(users);
            });
            //로그인 DB 조회 후 없으면 생성     
            signInRepository.findByUsername(email).orElseGet(() -> {
                SignIn signIn = new SignIn();
                signIn.setUsername(email);
                signIn.setPassword("{noop}");
                signIn.setUsers(user);
                return signInRepository.save(signIn);
            });

            //JWT 발급
            String accessJwt = JwtUtil.createAccessToken(user.getUserId(), user.getEmail());
            String refreshJwt = JwtUtil.createRefreshToken(user.getUserId());

            return new OAuthSignInResponse(
                    accessJwt, refreshJwt, user.getUserId(), email, user.getNickname(), "NAVER"
            );

        } catch (Exception e) {
            throw new AuthErrorException(OAUTH_PROVIDER_ERR);
        }

    }

    // 네이버 프로필에서 응답받은 생년월일 LocalDate로 바꿔주는 함수
    private LocalDate toBirthDate(String year, String monthDay) {
        if (year == null || monthDay == null)
            return null;//연도나 생일 정보가 없으면 그냥 null 반환.
        String[] md = monthDay.split("-");//"05-21" 같은 문자열을 "-" 기준으로 잘라서
        if (md.length != 2)//잘못된 형식이면 null 처리.
            return null;
        try {
            return LocalDate.of(
                    Integer.parseInt(year), // 2000
                    Integer.parseInt(md[0]), // 02 (월)
                    Integer.parseInt(md[1])// 19 (일)
            );
        } catch (Exception e) {
            return null;
        }
    }

    // 네이버 프로필에서 응답 받은 전화번호 정규화작업
    private String normalizePhone(String mobile) {
        if (mobile == null)//전화번호가 아예 없으면 null 반환
            return null;
        String digits = mobile.replaceAll("[^0-9]", ""); //정규식 [^0-9] = 숫자가 아닌 모든 문자,전부 제거해서 숫자만 남김
        if (digits.length() == 11 && digits.startsWith("010")) {
            return digits; // 01012345678 형태
        }
        return digits;
    }
}

