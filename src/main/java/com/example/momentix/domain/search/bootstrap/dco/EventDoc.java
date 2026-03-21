package com.example.momentix.domain.search.bootstrap.dco;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

//적재할 문서
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventDoc {
    // 검색/필터용 기본 정보
    private final String eventId;
    private final String eventTitle;
    private final String eventCategory;
    private final String eventStartDate;
    private final String eventEndDate;
    private final String placeName;
    private final String placeAddress;

    // 자동완성(completion)
    @JsonProperty("eventTitle_suggest")
    private final SuggestField eventTitleSuggest;

    @JsonProperty("placeName_suggest")
    private final SuggestField placeNameSuggest;

    public EventDoc(
            String eventId,
            String eventTitle,
            String eventCategory,
            String eventStartDate,
            String eventEndDate,
            String placeName,
            String placeAddress,
            SuggestField eventTitleSuggest,
            SuggestField placeNameSuggest
    ) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.eventCategory = eventCategory;
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
        this.placeName = placeName;
        this.placeAddress = placeAddress;
        this.eventTitleSuggest = eventTitleSuggest;
        this.placeNameSuggest = placeNameSuggest;
    }
    // 자동완성에 들어갈 값들
    @Getter
    public static class SuggestField {
        private final List<String> input;
        private final Integer weight;

        public SuggestField(List<String> input, Integer weight) {
            this.input = input;
            this.weight = weight;
        }
    }
}
