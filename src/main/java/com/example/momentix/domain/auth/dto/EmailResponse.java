package com.example.momentix.domain.auth.dto;

public class EmailResponse {
    private String email;

    public EmailResponse(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
