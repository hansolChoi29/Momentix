package com.example.momentix.domain.events.dto.request;


import com.example.momentix.domain.events.entity.enums.AgeRatingType;
import com.example.momentix.domain.events.entity.enums.EventCategoryType;
import com.example.momentix.domain.events.entity.eventtimes.EventTimes;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class CreateEventsRequestDto {
    private String eventTitle;
    private EventCategoryType eventCategory;
    private AgeRatingType ageRating;
    private LocalDate eventStartDate;
    private LocalDate eventEndDate;

    private String placeName;
    private String placeAddress;


    private List<EventTimeRequestDto> eventTimeList;

    private LocalDateTime reservationStartTime;
    private LocalDateTime reservationEndTime;

    private String castName;
}


