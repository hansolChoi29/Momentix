package com.example.momentix.domain.events.service;

import com.example.momentix.domain.events.dto.request.*;
import com.example.momentix.domain.events.dto.response.AllReadEventsResponseDto;
import com.example.momentix.domain.events.dto.response.EventsResponseDto;
import com.example.momentix.domain.events.dto.response.ReadEventResponseDto;
import com.example.momentix.domain.events.entity.EventCast;
import com.example.momentix.domain.events.entity.EventPlace;
import com.example.momentix.domain.events.entity.Events;
import com.example.momentix.domain.events.entity.casts.Casts;
import com.example.momentix.domain.events.entity.eventtimes.EventTimes;
import com.example.momentix.domain.events.entity.places.Places;
import com.example.momentix.domain.events.entity.reservationtimes.ReservationTimes;
import com.example.momentix.domain.events.repository.EventsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.momentix.domain.common.exception.event.EventErrorCode.*;

import com.example.momentix.domain.common.exception.event.EventErrorException;


import java.util.List;

@Service
@RequiredArgsConstructor
public class EventsService {
    private final EventsRepository eventsRepository;
    private final PlaceService placeService;
    private final CastService castService;

    @Transactional
    public EventsResponseDto createEvent(CreateEventsRequestDto requestDto) {

        // Events(공연) 테이블 기본 정보로 생성
        Events createEvent = Events.builder()
                .eventTitle(requestDto.getEventTitle())
                .ageRatingType(requestDto.getAgeRating())
                .eventCategoryType(requestDto.getEventCategory())
                .eventStartDate(requestDto.getEventStartDate())
                .eventEndDate(requestDto.getEventEndDate())
                .build();

        // Places(공연장) 공연장 정보로 검색 or 생성
        Places places = placeService.createPlace(
                new PlacesRequestDto(requestDto.getPlaceName(), requestDto.getPlaceAddress()));

        // EventTimes(공연 시간) 생성
        for (EventTimeRequestDto eventTimesRequest : requestDto.getEventTimeList()) {
            EventTimes eventTimes = EventTimes.builder()
                    .eventStartTime(eventTimesRequest.getEventStartTime())
                    .eventEndTime(eventTimesRequest.getEventEndTime())
                    .build();
            createEvent.addEventTime(eventTimes);
        }

        // ReservationTimes(예매 시간) 생성
        ReservationTimes reservationTimes = ReservationTimes.builder()
                .reservationStartTime(requestDto.getReservationStartTime())
                .reservationEndTime(requestDto.getReservationEndTime())
                .events(createEvent)
                .build();

        // Casts(출연자) 출연자 정보로 검색 or 생성

        Casts casts = castService.createCast(new CreateCastRequestDto(requestDto.getCastName(), ""));

        // EventPlace(공연, 공연장소 중간테이블), EventTime(공연시간), ReservationTime(예매시간), EventCast(공연, 출연자 중간테이블) 저장
        // 연관관계 편의 메서드 사용
        createEvent.addEventInfo(
                EventPlace.builder()
                        .events(createEvent)
                        .places(places)
                        .build(),
                reservationTimes,
                EventCast.builder()
                        .events(createEvent)
                        .casts(casts)
                        .build()
        );
        // Events(공연) 저장
        eventsRepository.save(createEvent);

        return new EventsResponseDto(createEvent, places, reservationTimes, casts);

    }

    // 공연 전체 조회
    @Transactional(readOnly = true)
    public Page<AllReadEventsResponseDto> allReadEvents(Pageable pageable) {
        List<AllReadEventsResponseDto> allReadResponse = eventsRepository.AllReadEvents();
        return new PageImpl<>(allReadResponse, pageable, allReadResponse.size());
    }

    // 공연에 관련된 기본적인 내용들 한번에 수정
    @Transactional
    public void updateEvent(Long eventId, UpdateBaseEventRequestDto requestDto) {
        Events updateEvent =
                eventsRepository.findById(eventId).orElseThrow(() -> new EventErrorException(EVENT_NOT_FOUND));

        updateEvent.setEvent(
                requestDto.getEventTitle(),
                requestDto.getAgeRating(),
                requestDto.getEventCategory(),
                requestDto.getEventStartDate(),
                requestDto.getEventEndDate());
        eventsRepository.save(updateEvent);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Events deleteEvent =
                eventsRepository.findById(eventId).orElseThrow(() -> new EventErrorException(EVENT_NOT_FOUND));
        deleteEvent.setDeleted(true);
        eventsRepository.save(deleteEvent);
    }

    // 공연 단건 조회
    @Transactional(readOnly = true)
    public ReadEventResponseDto readEvent(Long eventId, Long placeId) {
        ReadEventResponseDto readResponse = eventsRepository.searchEventById(eventId, placeId);
        return readResponse;
    }
}
