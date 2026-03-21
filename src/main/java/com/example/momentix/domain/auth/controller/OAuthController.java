package com.example.momentix.domain.auth.controller;


import com.example.momentix.domain.auth.dto.response.OAuthSignInResponse;
import com.example.momentix.domain.auth.entity.OAuthProvider;
import com.example.momentix.domain.auth.service.oauth.OAuthService;
import com.example.momentix.domain.auth.service.oauth.OAuthServiceFactory;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/auth/sign-in")
@RequiredArgsConstructor
public class OAuthController {
    private final OAuthServiceFactory oAuthServiceFactory;

    @Value("${naver.client.id}")
    private String naverClientId;

    @Value("${naver.redirect.uri}")
    private String naverRedirectUri;


    @Value("${kakao.client.id}")
    private String kakaoClientId;

    @Value("${kakao.redirect.uri}")
    private String kakaoRedirectUri;


    @GetMapping("/naver")
    public ResponseEntity<Void> naver(HttpSession session) {
        String state = UUID.randomUUID().toString();
        session.setAttribute("OAUTH_STATE_NAVER", state);
        //url 조합
        String url = UriComponentsBuilder
                .fromHttpUrl("https://nid.naver.com/oauth2.0/authorize")// 네이버 로그인 인증 엔드포인트
                .queryParam("response_type", "code") // OAuth2 표준: code 방식
                .queryParam("client_id", naverClientId)// 네이버 애플리케이션 Client ID
                .queryParam("redirect_uri", naverRedirectUri) // 네이버 콘솔과 완전 동일해야 함 (인코딩 주의)
                .queryParam("state", state) // CSRF 방지용, 동적 state
                .build(false) // redirect_uri에 이미 인코딩 되어 있으면 false
                .toUriString();
        return ResponseEntity.status(302).location(URI.create(url)).build();
    }

    //------------카카오---------
    @GetMapping("/kakao")
    public ResponseEntity<Void> kakao(HttpSession session) {
        String state = UUID.randomUUID().toString();
        session.setAttribute("OAUTH_STATE_KAKAO", state);

        URI uri = UriComponentsBuilder
                .fromHttpUrl("https://kauth.kakao.com/oauth/authorize")//카카오 로그인 인증 요청 URL (사용자가 로그인/동의창 보게 만드는 엔드포인트)
                .queryParam("response_type", "code")//인가 코드(authorization code) 방식 쓰겠다 선언
                .queryParam("client_id", kakaoClientId)
                .queryParam("redirect_uri", kakaoRedirectUri)
                .queryParam("state", state)
                .queryParam("scope", "profile_nickname account_email")//요청할 사용자 정보 범위
                .encode()//지금까지 만든 URL을 인코딩
                .build()
                .toUri();

        return ResponseEntity.status(302).location(uri).build();
    }

    // auth/sign-in/oauth/callback/provider → code를 받아서 토큰 발급 요청 → 프로필 조회 → DB 저장 → JWT 리턴
    @GetMapping("/callback/naver")
    public OAuthSignInResponse callback(
            @RequestParam("code") String code,
            @RequestParam("state") String stateParam
    ) {
        OAuthService service = oAuthServiceFactory.getOAuthService(OAuthProvider.NAVER);
        return service.signIn(code, stateParam);
    }

    @GetMapping("/callback/kakao")
    public OAuthSignInResponse kakaoCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String stateParam,
            HttpSession session
    ) {
        String expected = (String) session.getAttribute("OAUTH_STATE_KAKAO");
        session.removeAttribute("OAUTH_STATE_KAKAO");
        if (expected != null && !expected.equals(stateParam)) {
            throw new IllegalArgumentException("CSRF 의심: state 불일치(KAKAO)");
        }
        OAuthService svc = oAuthServiceFactory.getOAuthService(OAuthProvider.KAKAO);
        return svc.signIn(code, stateParam); // null 가능
    }
}