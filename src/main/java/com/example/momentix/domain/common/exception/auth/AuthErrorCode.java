package com.example.momentix.domain.common.exception.auth;


public enum AuthErrorCode {

    BAD_REQUEST(400, "비밀번호 또는 아이디가 일치하지 않습니다."),
    UNAUTHORIZED(401, "다시 로그인해 주세요."),
    FORBIDDEN(403, "접근 권한이 없습니다."),
    NOT_FOUND(404, "존재하지 않는 사용자입니다."),
    INTERNAL_SERVER_ERROR(500, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."),
    CONFLICT(409, "이미 가입한 이메일입니다."),
    HOST_ID_NOT_AVAILABLE(409, "생성 가능한 호스트 아이디가 없습니다."),
    BLACK_USER(403, "블랙리스트 계정 또는 탈퇴한 계정입니다."),
    EMAIL_CODE_REQUEST_TOO_FREQUENT(429, "잠시 후에 다시 시도해 주세요."),
    EMAIL_CODE_INVALID(400, "인증에 실패했습니다. 코드를 다시 요청해 주세요."),
    EMAIL_TOKEN_EXPIRED(401, "인증 토큰이 만료되었거나 이미 사용되었습니다."),
    AUTHENTICATION_REQUIRED(401, "인증이 필요합니다."),

    OAUTH_STATE_MISMATCH(401, "요청 상태값이 일치하지 않습니다."),
    OAUTH_TOKEN_EXCHANGE_FAILED(400, "소셜 토큰 발급에 실패했습니다."),
    OAUTH_PROFILE_FETCH_FAILED(400, "소셜 프로필 조회에 실패했습니다."),
    OAUTH_EMAIL_SCOPE_REQUIRED(400, "제공 동의가 필요합니다."),
    OAUTH_PROVIDER_ERROR(502, "소셜 제공자 응답 처리 중 오류가 발생했습니다."),

    OAUTH_PROVIDER_ERR(502, "처리 중 오류가 발생했습니다."),
    UNSUPPORTED_OAUTH_PROVIDER(400, "지원하지 않는 소셜 프로바이더입니다.");

    private final int status;
    private final String message;

    AuthErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
