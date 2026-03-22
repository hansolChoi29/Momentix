package com.example.momentix.domain.users.controller;

import com.example.momentix.domain.users.dto.ReadUserSimpleResponseDto;
import com.example.momentix.domain.users.dto.UserRequestDto;
import com.example.momentix.domain.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Tag(name = "User", description = "회원 관련 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "회원 탈퇴", description = "비밀번호 확인 후 탈퇴 처리")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdrawMe(
            @AuthenticationPrincipal String email,
            @RequestBody UserRequestDto userDeleteRequestDto
    ) {
        userService.withdrawSelf(email, userDeleteRequestDto.getPassword());
    }

    @Operation(summary = "개인정보 수정", description = "닉네임, 전화번호, 비밀번호 변경 가능")
    @PutMapping("/user-info")
    public void updateUserInfo(
            @AuthenticationPrincipal String email,
            @RequestBody UserRequestDto userRequestDto
    ) {
        userService.updateUserInfo(email, userRequestDto);
    }

    @Operation(summary = "회원 정보 조회", description = "본인 또는 ADMIN만 조회 가능")
    @GetMapping("/{userId}")
    public ResponseEntity<ReadUserSimpleResponseDto> readUserSimple(
            @PathVariable Long userId,
            @AuthenticationPrincipal String email
    ) {
        ReadUserSimpleResponseDto readUserSimpleResponseDto = userService.readUserSimple(userId, email);
        return new ResponseEntity<>(readUserSimpleResponseDto, HttpStatus.OK);
    }
}
