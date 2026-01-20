package com.example.momentix.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    public interface UserSignUp {
    }

    public interface HostSignUp {
    }

    @NotBlank(groups = UserSignUp.class, message = "비밀번호는 필수입니다.")
    @Null(groups = HostSignUp.class, message = "호스트 회원가입은 비밀번호를 입력하지 않습니다.")
    private String password;

    @NotBlank(groups = UserSignUp.class, message = "비밀번호 확인은 필수입니다.")
    @Null(groups = HostSignUp.class, message = "호스트 회원가입은 비밀번호를 입력하지 않습니다.")
    private String confirmPassword;

    @NotBlank(groups = UserSignUp.class, message = "닉네임은 3~10자리이어야 합니다.")
    @Null(groups = HostSignUp.class, message = "호스트 회원가입은 닉네임을 입력하지 않습니다.")
    private String nickname;

    @NotBlank(groups = UserSignUp.class)
    @Pattern(regexp = "\\d{8}", message = "생년월일은 8자리여야 합니다.")
    @Null(groups = HostSignUp.class, message = "호스트 회원가입은 생년월일을 입력하지 않습니다.")
    private String birthDate;

    @NotBlank(groups = UserSignUp.class)
    @Pattern(groups = UserSignUp.class, regexp = "\\d{10,11}", message = "휴대폰번호는 11자리 숫자여야 합니다.")
    @Null(groups = HostSignUp.class, message = "호스트 회원가입은 휴대폰번호를 입력하지 않습니다.")
    private String phoneNumber;

    @NotBlank(groups = HostSignUp.class)
    @Pattern(groups = HostSignUp.class, regexp = "\\d{10}", message = "사업자번호는 10자리 숫자여야 합니다.")
    @Null(groups = UserSignUp.class, message = "일반 회원가입은 사업자번호를 입력하지 않습니다.")
    private String businessNumber;
}
