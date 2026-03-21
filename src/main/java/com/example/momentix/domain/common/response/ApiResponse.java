package com.example.momentix.domain.common.response;


public class ApiResponse<T> {
    private final T data;
    private final String message;

    public ApiResponse(String message, T data) {
        this.data = data;
        this.message = message;
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(message, data);
    }

    public static <T> ApiResponse<Object> fail(String message, T data) {
        return new ApiResponse<>(message, data);
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}