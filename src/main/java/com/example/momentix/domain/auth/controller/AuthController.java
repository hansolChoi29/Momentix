package com.example.momentix.domain.auth.controller;


import com.example.momentix.domain.auth.dto.*;
import com.example.momentix.domain.auth.dto.command.EmailCommand;
import com.example.momentix.domain.auth.dto.command.SignInCommand;
import com.example.momentix.domain.auth.dto.request.EmailVerifyRequest;
import com.example.momentix.domain.auth.dto.request.SignUpRequest;
import com.example.momentix.domain.auth.dto.response.SignUpResponse;
import com.example.momentix.domain.auth.service.EmailVerificationService;
import com.example.momentix.domain.auth.service.SignInService;
import com.example.momentix.domain.auth.service.SignOutService;
import com.example.momentix.domain.auth.service.SignUpService;
import com.example.momentix.domain.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Auth", description = "인증 관련 API")
@RequestMapping("/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {
    private final SignInService signInService;
    private final SignUpService signUpService;
    private final EmailVerificationService emailVerificationService;
    private final SignOutService signOutService;

    @Operation(summary = "로그인", description = "AccessToken 반환, RefreshToken은 쿠키에 저장")
    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<String>> signIn(
            @RequestBody SignInRequest signInRequest
    ) {
        SignInCommand signin = new SignInCommand(
                signInRequest.getUsername(),
                signInRequest.getPassword()
        );
        SignInDto tokens = signInService.signIn(signin);

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.ok(tokens.getAccessToken(), "로그인!"));
    }

    //이메일 인증(회원가입 전 단계 - consumer)
    @Operation(summary = "이메일 인증 코드 발송", description = "회원가입 전 이메일 인증 코드 발송")
    @PostMapping("/sign-up/email-verification")
    public ResponseEntity<ApiResponse<Object>> requestEmailCode(
            @RequestBody EmailVerifyRequest request
    ) {
        EmailCommand command = new EmailCommand(request.getEmail());
        emailVerificationService.sendCode(command);

        return ResponseEntity.ok(ApiResponse.ok(null, "인증 코드를 확인해 주세요."));
    }

    // 코드확인 및 검증 토큰 발급
    @Operation(summary = "이메일 인증 코드 확인", description = "코드 확인 후 검증 토큰 발급")
    @PostMapping("/sign-up-verify/email-verification")
    public ResponseEntity<ApiResponse<EmailVerifyConfirmResponse>> confirmEmailCode(
            @RequestBody EmailVerifyConfirm request
    ) {
        EmailVerifyConfirmCommand command = new EmailVerifyConfirmCommand(
                request.getEmail(), request.getCode()
        );
        EmailVerifyConfirmDto token = emailVerificationService.confirmAndIssueToken(command);
        EmailVerifyConfirmResponse response = new EmailVerifyConfirmResponse(token.getToken());

        return ResponseEntity.ok(ApiResponse.ok(response, "인증코드"));
    }

    @Operation(summary = "토큰 재발급", description = "쿠키의 RefreshToken으로 새 AccessToken 발급")
    @PostMapping("/refresh")
    public ResponseEntity<TokenRes> refresh(
            @CookieValue(value = "REFRESH_TOKEN", required = false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SignInDto result = signInService.refresh(refreshToken);

        return ResponseEntity.ok(new TokenRes(result.getAccessToken(), null));
    }

    @Operation(summary = "로그아웃", description = "쿠키의 RefreshToken 삭제")
    @PostMapping("/sign-out")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void signOut(
            HttpServletResponse response
    ) {
        signOutService.signOut(response);
    }

    @Operation(summary = "일반 회원가입", description = "이메일 인증 토큰을 X-Email-Verify-Token 헤더에 담아 요청")
    @PostMapping("/sign-up/user")
    public ResponseEntity<SignUpResponse> signUpUser(
            @Validated(SignUpRequest.UserSignUp.class) @RequestBody SignUpRequest req,
            @RequestHeader(value = "X-Email-Verify-Token", required = false) String emailTokenHeader
    ) {

        String token = extractBearer(emailTokenHeader);
        String email = emailVerificationService.consumerVerifiedToken(token); // Redis에서 email 복구

        Long userId = signUpService.signUpUser(email, req);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SignUpResponse("회원가입 성공", userId));
    }

    @Operation(summary = "호스트 회원가입", description = "사업자번호 입력, 자동 생성된 ID/PW 반환")
    @PostMapping("/sign-up/host")
    public ResponseEntity<Map<String, String>> signUpHost(
            @Validated(SignUpRequest.HostSignUp.class)
            @RequestBody SignUpRequest req) {
        Map<String, String> creds = signUpService.signUpHost(req);

        Map<String, String> body = new HashMap<>();
        body.put("message", "회원가입 성공! 아이디와 비밀번호는 일치합니다. 비밀번호를 변경해 주세요.");
        body.put("username", creds.get("username"));
        body.put("password", creds.get("password"));

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    private String extractBearer(
            String authHeader
    ) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("유효하지 않은 Authorization 헤더");
    }

    public record SigninReq(String username, String password) {
    }

    public record TokenRes(String accessToken, String refreshToken) {
    }
}
