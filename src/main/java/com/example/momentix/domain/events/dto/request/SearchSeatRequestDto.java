package com.example.momentix.domain.events.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchSeatRequestDto {
    private Long eventId;
    private Long placeId;
    private Long eventTimeId;

    public SearchSeatRequestDto(Long eventId, Long placeId, Long eventTimeId) {
        this.eventId = eventId;
        this.placeId = placeId;
        this.eventTimeId = eventTimeId;
    }
}
