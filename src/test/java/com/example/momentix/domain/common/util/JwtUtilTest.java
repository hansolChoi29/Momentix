package com.example.momentix.domain.common.util;

import org.junit.jupiter.api.BeforeEach;

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

    /*
    createRefreshToken
    1. refreshToken의 타입은 refresh
    2. refreshToken에서 userId 추출 가능
    */

    /*
    validateToken
    1. 유효한 토큰은 validateToken이 true 반환
    2. 만료된 토큰은 validateToken이 false 반환
    3. 위변조된 토큰은 validateToken이 false 반환
    */

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