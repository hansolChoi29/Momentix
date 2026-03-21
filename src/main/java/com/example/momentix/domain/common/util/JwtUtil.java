package com.example.momentix.domain.common.util;


import com.example.momentix.domain.auth.entity.RoleType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

public class JwtUtil {
    private static final long ACCESS_TOKEN = 1000L * 60 * 30;
    private static final long REFRESH_TOKEN = 1000L * 60 * 60 * 24 * 7;
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private static SecretKey secretKey;// 비밀키(서버만 알고있어야 하는 키)

    public static void init(String secret) {
        secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public static String createAccessToken(
            Long userId,
            String email,
            RoleType role
    ) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("role", role.name())
                .claim("typ", "access") // 토큰 타입 명시해 주는 걸 권장한다고 함
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String createRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject("refresh")
                .claim("userId", userId)
                .claim("typ", "refresh")
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;


        } catch (ExpiredJwtException e) {
            log.warn("토큰 만료됨: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("토큰 검증 실패: {}", e.getMessage());
        }
        return false;
    }

    private static Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static String getUserEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public static String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    public static String getTokenType(String token) {
        return getClaims(token).get("typ", String.class);
    }

    public static Authentication getAuthenticationFromToken(String token) {
        String email = getUserEmailFromToken(token);
        String role = getRoleFromToken(token);

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(authority)
        );
    }

    public static boolean isRefreshToken(String token) {
        return "refresh".equals(getTokenType(token));
    }

    public static Long getUserIdFromToken(String token) {
        Number n = getClaims(token).get("userId", Number.class);
        return (n == null) ? null : n.longValue();
    }
}
