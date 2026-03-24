package com.example.momentix.domain.common.util;

import com.example.momentix.domain.auth.entity.RoleType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


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


    /*
    createAccessToken
    1. accessToken 발급 시 subject는 email, claim에 userId와 role이 담김
    2. accessToken의 타입은 access
    */
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

    /*
    createRefreshToken
    1. refreshToken의 타입은 refresh
    2. refreshToken에서 userId 추출 가능
    */
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

    /*
    validateToken
    1. 유효한 토큰은 validateToken이 true 반환
    2. 만료된 토큰은 validateToken이 false 반환
    3. 위변조된 토큰은 validateToken이 false 반환
    */
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
    /*
    isRefreshToken
    1. refreshToken으로 isRefreshToken 호출하면 true
    2. accessToken으로 isRefreshToken 호출하면 false
    */

    /*
    getAuthenticationFromToken
    1. accessToken으로 Authentication 객체 생성 시 principal은 email, authority는 ROLE_CONSUMER
    */
}