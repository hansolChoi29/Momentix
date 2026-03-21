package com.example.momentix.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpResponse {
    Long userId;
    String message;

    public SignUpResponse(String message, Long userId) {
        this.message = message;
        this.userId = userId;
    }
}
