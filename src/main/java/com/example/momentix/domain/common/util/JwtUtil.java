package com.example.momentix.domain.common.util;


import com.example.momentix.domain.auth.entity.RoleType;
import com.example.momentix.domain.auth.impl.UserDetailsServiceImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    private static SecretKey secretKey;// 비밀키(서버만 알고있어야 하는 키)

    private static final long ACCESS_TOKEN = 1000L * 60 * 30;
    private static final long REFRESH_TOKEN = 1000L * 60 * 60 * 24 * 7;

    //  초기화
    public static void init(String secret) {
        secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    //어세스 토큰 생성
    public static String createAccessToken(Long userId, String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("typ", "access") // 토큰 타입 명시해 주는 걸 권장한다고 함
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // RefreshToken 생성
    public static String createRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject("refresh")
                .claim("userId", userId)
                .claim("typ", "refresh")
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 유효성 검사
    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("토큰 만료됨: " + e.getMessage());
        } catch (JwtException e) {
            System.out.println("토큰 검증 실패: " + e.getMessage());
        }
        return false;
    }

    // 클레임이란? Payload에 담긴 key-value 쌍 데이터
    // JWT 구조는 보통 Header.Payload.Signature 3부분으로 구성
    // Header: 토큰 타입, 해시 알고리즘
    //Payload (Claims): 토큰에 담긴 실제 데이터 (ex. userId, role, 만료시간 등)
    //Signature: Header + Payload + secretKey를 해시해서 만든 값 (위변조 방지용)
    // JWT 토큰에서 Claims(클레임, 토큰에 담긴 실제 데이터) 를 꺼내오는 역할
    private static Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey) // 토큰 서명 검증에 사용할 비밀키 등록
                .build()
                .parseClaimsJws(token) // 전달 받은 토큰 검증 + 파싱
                .getBody(); // 토큰의 페이로드(클레임) 반환
    }

    // 이메일(subject) 꺼내기
    public static String getUserEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    // role 꺼내기
    public static String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    public static String getTokenType(String token) {
        return getClaims(token).get("typ", String.class);
    }

    // 토큰만으로 Authentication 생성 (DB 조회 X)
    public static Authentication getAuthenticationFromToken(String token, UserDetailsServiceImpl userDetailsServiceImpl) {
        String username = getUserEmailFromToken(token);
        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    // 리프레시 엔드포인트
    public static boolean isRefreshToken(String token) {
        return "refresh".equals(getTokenType(token));
    }

    public static Long getUserIdFromToken(String token) {
        Number n = getClaims(token).get("userId", Number.class);
        return (n == null) ? null : n.longValue();
    }
}
