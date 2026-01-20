package com.example.momentix.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerifyConfirmResponse {
    // 검증 성공하고 토큰 응답용
    private String emailVerifiedToken;
    public String getEmailVerifiedToken(){
        return emailVerifiedToken;
    }
}
// 3) 서버가 클라이언트에 검증 토큰 응답(EmailVerifyConfirmResponse)
//이후 회원가입 시 이 토큰을 헤더(X-Email-Verify-Token)로 보내서 인증된 이메일을 복구

// X-Email-Verify-Token: 서버가 이메일 인증 성공 후 발급해 주는 임시 인증 토큰(UUID)을 담는 HTTP 헤더 이름
