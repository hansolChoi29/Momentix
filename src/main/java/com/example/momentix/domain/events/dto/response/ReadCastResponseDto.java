package com.example.momentix.domain.events.dto.response;

import lombok.Getter;

@Getter
public class ReadCastResponseDto {
    private String castName;
    private String castImageUrl;

    public ReadCastResponseDto(
            String castName,
            String castImageUrl
    ) {
        this.castName = castName;
        this.castImageUrl = castImageUrl;
    }
}
