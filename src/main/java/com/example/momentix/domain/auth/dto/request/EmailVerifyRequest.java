package com.example.momentix.domain.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerifyRequest {
    //이메일 인증 코드 요청용
    private String email;
}
// 1) 클라이언트가 서버에 이메일 전송(EmailVerifyRequest)
//서버가 인증코드(6자리) 발급해서 메일 전송, Redis에 저장
