package com.example.momentix.domain.users.service;

import com.example.momentix.domain.auth.entity.RoleType;
import com.example.momentix.domain.auth.entity.SignIn;
import com.example.momentix.domain.auth.repository.SignInRepository;
import com.example.momentix.domain.common.exception.auth.AuthErrorException;
import com.example.momentix.domain.common.exception.users.UsersErrorException;
import com.example.momentix.domain.users.dto.ReadUserSimpleResponseDto;
import com.example.momentix.domain.users.dto.UserRequestDto;
import com.example.momentix.domain.users.entity.Users;
import com.example.momentix.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.momentix.domain.common.exception.auth.AuthErrorCode.*;
import static com.example.momentix.domain.common.exception.users.UsersCode.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final SignInRepository signInRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void withdrawSelf(String username, String rawPassword) {
        // 1) 계정 조회 (isDeleted=false)
        SignIn signIn = signInRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new UsersErrorException(USER_NOT_FOUND));

        // 2) 비밀번호 확인
        if (!passwordEncoder.matches(rawPassword, signIn.getPassword())) {
            throw new UsersErrorException(INCORRECT_PASSWORD);
        }

        // 3) Users 하드 삭제 + SignIn tombstone(소프트딜리트)
        Users users = signIn.getUser();
        if (users == null) {
            throw new AuthErrorException(BLACK_USER);
        }

        // SignIn을 tombstone(소프트딜리트) 상태로
        signIn.markAsWithdrawn();

        // Users는 PII 제거 목적의 하드 삭제
        userRepository.delete(users);
    }

    // 닉넴/휴대폰/새 비번 한 번에 수정
    @Transactional
    public void updateUserInfo(String username, UserRequestDto userRequestDto) {
        SignIn signIn = signInRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new UsersErrorException(INCORRECT_PASSWORD));

        Users users = signIn.getUser();
        if (users == null) {
            throw new UsersErrorException(INCORRECT_PASSWORD);
        }
        boolean changed = false;

        // 닉네임
        if (userRequestDto.getNickname() != null && !userRequestDto.getNickname().isBlank()
                && !userRequestDto.getNickname().equals(users.getNickname())) {
            users.setNickname(userRequestDto.getNickname());
            changed = true;
        }
        // 휴대폰 (유효성은 400 유지)
        if (userRequestDto.getPhoneNumber() != null && !userRequestDto.getPhoneNumber().isBlank()
                && !userRequestDto.getPhoneNumber().equals(users.getPhoneNumber())) {
            String p = userRequestDto.getPhoneNumber();
            if (!p.matches("^[0-9]{10,11}$")) {
                throw new UsersErrorException(INVALID_PHONE_NUMBER_FORMAT);
            }
            users.setPhoneNumber(p);
            changed = true;
        }
        // 새 비밀번호(둘 다 있을 때만 변경) → 비번 유효성 관련은 401
        if (userRequestDto.getNewPassword() != null || userRequestDto.getNewConfirmPassword() != null) {
            if (userRequestDto.getNewPassword() == null || userRequestDto.getNewConfirmPassword() == null) {
                throw new UsersErrorException(PASSWORD_MISMATCH);
            }
            if (!userRequestDto.getNewPassword().equals(userRequestDto.getNewConfirmPassword())) {
                throw new UsersErrorException(INCORRECT_PASSWORD);
            }
            signIn.setPassword(passwordEncoder.encode(userRequestDto.getNewPassword()));
            changed = true;
        }

        // 변경사항 없음 → 401
        if (!changed) {
            throw new UsersErrorException(NO_CHANGES_TO_UPDATE);
        }
        userRepository.save(users);
        signInRepository.save(signIn);
    }

    @Transactional(readOnly = true)
    public ReadUserSimpleResponseDto readUserSimple(Long userId, String email) {
        SignIn signIn = signInRepository.findByUsername(email)
                .orElseThrow(() -> new AuthErrorException(AUTHENTICATION_REQUIRED));

        boolean isAdmin = signIn.getUser().getRole() == RoleType.ADMIN;
        boolean isSelf = signIn.getUser().getUserId().equals(userId);

        if (!isAdmin && !isSelf) {
            throw new AuthErrorException(FORBIDDEN);
        }

        Users users = userRepository.findWithSignInByUserId(userId)
                .orElseThrow(() -> new UsersErrorException(USER_NOT_FOUND));

        return ReadUserSimpleResponseDto.from(users);
    }
}
