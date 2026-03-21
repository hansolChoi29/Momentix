package com.example.momentix.domain.common.exception.response;

public class ErrorResponseDto {
    private int status;
    private String message;

    public ErrorResponseDto(int status, String message) {
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
