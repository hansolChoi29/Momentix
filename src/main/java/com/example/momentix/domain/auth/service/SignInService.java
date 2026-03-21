package com.example.momentix.domain.auth.service;


import com.example.momentix.domain.auth.dto.command.SignInCommand;
import com.example.momentix.domain.auth.dto.SignInDto;
import com.example.momentix.domain.auth.entity.SignIn;
import com.example.momentix.domain.auth.repository.SignInRepository;
import com.example.momentix.domain.common.util.JwtUtil;
import com.example.momentix.domain.users.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.momentix.domain.common.exception.auth.AuthErrorException;

import static com.example.momentix.domain.common.exception.auth.AuthErrorCode.*;


@Service
@RequiredArgsConstructor
public class SignInService {
    private final SignInRepository signInRepository;
    private final PasswordEncoder passwordEncoder;

    public SignInDto signIn(SignInCommand signInCommand) {
        // TODO : validator 생성 요망
        // TODO : UserDetailsImpl, UserDetailsServiceImpl 는
        //  “Spring Security가 로그인 검증할 때 쓰는 어댑터”

        // 유저객체가 왜 서비스에 있음;;
        // 로그인에서 어세스토큰과 리프레시토큰을 발급해야 하는데;;
        // TODO : username으로 DB에서 SignIn/User 조회
        // TODO : 비밀번호 매칭 확인(PasswordEncoder.matches)
        // TODO : 토큰(access/refresh) 생성해서 DTO로 반환
        // TODO : 명심 - Authentication / UserDetails / GrantedAuthority 호출 금지
        // 401가 왜 서비스에 있음?;;

        SignIn user = signInRepository.findByUsername(signInCommand.getUsername())
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));

        String accessToken = JwtUtil.createAccessToken(
                user.getSignInId(),
                user.getUsername(),
                user.getUser().getRole()
        );
        String refreshToken = JwtUtil.createRefreshToken(user.getSignInId());

        return new SignInDto(
                accessToken,
                refreshToken
        );
    }

    // 리프레시 엔드포인트
    public SignIn loadByUserId(Long userId) {
        return signInRepository.findById(userId)
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));
    }
}
