package com.example.momentix.domain.users.controller;

import com.example.momentix.domain.users.dto.ReadUserSimpleResponseDto;
import com.example.momentix.domain.users.dto.UserRequestDto;
import com.example.momentix.domain.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdrawMe(
            @AuthenticationPrincipal String email,
            @RequestBody UserRequestDto userDeleteRequestDto
    ) {
        userService.withdrawSelf(email, userDeleteRequestDto.getPassword());
    }

    @PutMapping("/user-info")
    public void updateUserInfo(
            @AuthenticationPrincipal String email,
            @RequestBody UserRequestDto userRequestDto
    ) {
        userService.updateUserInfo(email, userRequestDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ReadUserSimpleResponseDto> readUserSimple(
            @PathVariable Long userId,
            @AuthenticationPrincipal String email
    ) {
        ReadUserSimpleResponseDto readUserSimpleResponseDto = userService.readUserSimple(userId, email);
        return new ResponseEntity<>(readUserSimpleResponseDto, HttpStatus.OK);
    }
}
