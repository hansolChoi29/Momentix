package com.example.momentix.domain.auth.dto;

public class EmailVerifyConfirmDto {
    private String token;

    public EmailVerifyConfirmDto(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
