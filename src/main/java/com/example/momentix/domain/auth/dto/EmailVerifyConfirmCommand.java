package com.example.momentix.domain.auth.dto;

public class EmailVerifyConfirmCommand {
    private String email;
    private String code;

    public EmailVerifyConfirmCommand(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public String getCode() {
        return code;
    }
}
