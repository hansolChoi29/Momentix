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

    public SignInDto signIn(SignInCommand signInCommand) {
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
