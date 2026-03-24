package com.example.momentix.domain.common.util;

import com.example.momentix.domain.auth.entity.RoleType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;


import javax.crypto.SecretKey;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    private static final String SECRET = "momentix-test-secret-key-32bytes!!";

    @BeforeEach
    void setUp() {
        JwtUtil.init(SECRET);
    }

    @Test
    @DisplayName("accessToken_발급_시_subject는_email_claim에_userId와_role이_담김")
    void createAccessToken_claimsAreCorrect() {
        String token = JwtUtil.createAccessToken(1L, "test@test.com", RoleType.CONSUMER);

        assertThat(JwtUtil.getUserEmailFromToken(token)).isEqualTo("test@test.com");
        assertThat(JwtUtil.getRoleFromToken(token)).isEqualTo("CONSUMER");
        assertThat(JwtUtil.getUserIdFromToken(token)).isEqualTo(1L);
    }

    @Test
    @DisplayName("accessToken의_타입은_access")
    void createAccessToken_typeIsAccess() {
        String token = JwtUtil.createAccessToken(1L, "test@test.com", RoleType.CONSUMER);

        assertThat(JwtUtil.getTokenType(token)).isEqualTo("access");
    }

    @Test
    @DisplayName("refreshToken의_타입은_refresh")
    void createRefreshToken_typeIsRefresh() {
        String token = JwtUtil.createRefreshToken(1L);

        assertThat(JwtUtil.getTokenType(token)).isEqualTo("refresh");
    }

    @Test
    @DisplayName("refreshToken에서_userId_추출_가능")
    void createRefreshToken_userIdExtractable() {
        String token = JwtUtil.createRefreshToken(1L);

        assertThat(JwtUtil.getUserIdFromToken(token)).isEqualTo(1L);
    }

    @Test
    @DisplayName("유효한_토큰은_validateToken이_true_반환")
    void validationToken_validToken_returnsTure() {
        String token = JwtUtil.createAccessToken(1L, "test@test.com", RoleType.CONSUMER);

        assertThat(JwtUtil.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("만료된_토큰은_validateToken이_false_반환")
    void validateToken_expiredToken_returnsFalse() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        String expiredToken = Jwts.builder()
                .setSubject("test@test.com")
                .claim("typ", "access")
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertThat(JwtUtil.validateToken(expiredToken)).isFalse();
    }

    @Test
    @DisplayName("위변조된_토큰은_validateToken이_false_반환")
    void validateToken_tamperedToken_returnsFalse() {
        String token = JwtUtil.createAccessToken(1L, "test@test.com", RoleType.CONSUMER);
        String tampered = token + "tempered";

        assertThat(JwtUtil.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("refreshToken으로_isRefreshToken_호출하면_true")
    void isRefreshToken_withRefreshToken_returnsTrue() {
        String token = JwtUtil.createRefreshToken(1L);
        assertThat(JwtUtil.isRefreshToken(token)).isTrue();
    }

    @Test
    @DisplayName("accessToken으로 Authentication 객체 생성 시 principal은 email, authority는 ROLE_CONSUMER")
    void getAuthenticationFromToken_returnsCorrectAuthentication() {
        String token = JwtUtil.createAccessToken(1L, "test@test.com", RoleType.CONSUMER);

        Authentication auth = JwtUtil.getAuthenticationFromToken(token);

        assertThat(auth.getPrincipal()).isEqualTo("test@test.com");
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_CONSUMER");
    }
}