package com.example.momentix.domain.events.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class EventTimeRequestDto {
    @NotNull
    private LocalDateTime eventStartTime;

    @NotNull
    private LocalDateTime eventEndTime;
}
