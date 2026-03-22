package com.example.momentix.domain.events.service;

import com.example.momentix.domain.common.exception.event.EventErrorException;
import com.example.momentix.domain.events.dto.request.PlacesRequestDto;
import com.example.momentix.domain.events.dto.request.SearchSeatRequestDto;
import com.example.momentix.domain.events.dto.response.BaseSeatResponseDto;
import com.example.momentix.domain.events.dto.response.PartRowColSeatResponseDto;
import com.example.momentix.domain.events.dto.response.SeatResponseDto;
import com.example.momentix.domain.events.entity.EventSeat;
import com.example.momentix.domain.events.entity.Events;
import com.example.momentix.domain.events.entity.enums.SeatStatusType;
import com.example.momentix.domain.events.entity.eventtimes.EventTimeReserveSeat;
import com.example.momentix.domain.events.entity.eventtimes.EventTimes;
import com.example.momentix.domain.events.entity.places.Places;
import com.example.momentix.domain.events.entity.seats.Seats;
import com.example.momentix.domain.events.repository.EventPlaceRepository;
import com.example.momentix.domain.events.repository.EventSeatRepository;
import com.example.momentix.domain.events.repository.EventsRepository;
import com.example.momentix.domain.events.repository.eventtimes.EventTimeReserveSeatRepository;
import com.example.momentix.domain.events.repository.eventtimes.EventTimesRepository;
import com.example.momentix.domain.events.repository.places.PlacesRepository;
import com.example.momentix.domain.events.repository.seats.SeatsRepository;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import static com.example.momentix.domain.common.exception.event.EventErrorCode.*;

@Service
@RequiredArgsConstructor
public class SeatService {
    private final EventsRepository eventsRepository;
    private final PlacesRepository placesRepository;
    private final EventPlaceRepository eventPlaceRepository;
    private final EventSeatRepository eventSeatRepository;
    private final SeatsRepository seatsRepository;
    private final EventTimesRepository eventTimesRepository;
    private final EventTimeReserveSeatRepository eventTimeReserveSeatRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public List<SeatResponseDto> createSeat(
            MultipartFile seatFile,
            Long placeId,
            Long eventId
    ) {
        // 좌석 등급 설정할 공연이 맞는지 확인
        Events events = eventsRepository.findById(eventId).orElseThrow(() -> new EventErrorException(SEAT_NOT_FOUND));

        // 좌석 등급 설정할 공연장 확인
        Places places = placesRepository.findById(placeId).orElseThrow(() -> new EventErrorException(EVENT_NOT_FOUND));
        // 공연과 공연장 일치 확인
        if (!eventPlaceRepository.existsByEventsAndPlaces(events, places)) {
            throw new EventErrorException(NOT_MATCH);
        }

        try (Reader reader = new InputStreamReader(seatFile.getInputStream())) {
            // csv 파일을 List <Dto> 형태로 반환
            List<SeatResponseDto> seatList = new CsvToBeanBuilder<SeatResponseDto>(reader)
                    .withType(SeatResponseDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();
            // List를 반복문으로 하나씩 데이터 저장
            for (SeatResponseDto seatDto : seatList) {
                // 기본 좌석 데이터 (좌석 배치)랑 일치 여부 확인
                Seats baseSeat = seatsRepository.findBySeatRowAndSeatColAndPlaces_Id(
                        seatDto.getSeatRow(),
                        seatDto.getSeatCol(),
                        places.getId()).orElseThrow(() -> new EventErrorException(SEAT_NOT_FOUND));
                ;
                // EventSeat 테이블에 CSV 파일 내부 데이터 + 기본 좌석 id 저장
                EventSeat eventSeat = EventSeat.builder()
                        .seatGradeType(seatDto.getSeatGradeType())
                        .seatPartType(seatDto.getSeatPartType())
                        .seatPrice(seatDto.getSeatPrice())
                        .seat(baseSeat)
                        .build();
                events.addEventSeat(eventSeat); // events 테이블로 저장 (연관관계 편의 메서드 사용)
            }
            eventsRepository.save(events);
            // 저장된 EventTime(공연 회차)당  좌석별 예매상태 매칭 테이블 생성
            for (EventTimes eventTimes : events.getEventTimeList()) { // event에 저장된 eventTime
                for (EventSeat eventSeatDto : events.getEventSeatList()) { // event에 저장된 eventSeat 데이터를 eventTime에 매칭
                    EventTimeReserveSeat etrSeat = EventTimeReserveSeat.builder()
                            .eventTimes(eventTimes)
                            .eventSeat(eventSeatDto)
                            .seatReserveStatus(SeatStatusType.AVAILABLE)
                            .build();
                    eventTimes.addEventTimeReserveSeatList(etrSeat);
                }
                eventTimesRepository.save(eventTimes);
            }
            return seatList;
        } catch (IOException e) {
            throw new EventErrorException(IO_ERROR);
        }
    }

    @Transactional
    public List<BaseSeatResponseDto> createBaseSeat(MultipartFile baseSeatFile,
                                                    PlacesRequestDto placeRequest) {
        // 공연장 존재 여부 확인
        Places place = placesRepository.findByPlaceName(placeRequest.getPlaceName())
                .orElseThrow(() -> new EventErrorException(EVENT_NOT_FOUND));
        // 공연장 기본 좌석 배치도 유무 확인
        if (!place.getSeatList().isEmpty()) {
            throw new EventErrorException(SEAT_ALREADY_BOOKED);
        }

        try (Reader reader = new InputStreamReader(baseSeatFile.getInputStream())) {
            List<BaseSeatResponseDto> baseSeatList = new CsvToBeanBuilder<BaseSeatResponseDto>(reader)
                    .withType(BaseSeatResponseDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();
            for (BaseSeatResponseDto baseSeatDto : baseSeatList) {
                Seats placeSeat = Seats.builder()
                        .seatRow(baseSeatDto.getSeatRow())
                        .seatCol(baseSeatDto.getSeatCol())
                        .build();
                place.addSeats(placeSeat);
            }
            placesRepository.save(place);

            return baseSeatList;

        } catch (IOException e) {
            throw new EventErrorException(IO_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public Page<PartRowColSeatResponseDto> readSeatsPart(
            Long eventId, Long placeId, Long eventTimeId,
            Long partId, Long rowId, Long colId, Pageable pageable) {
        // 공연 확인
        Events events = eventsRepository.findById(eventId).orElseThrow(() -> new EventErrorException(EVENT_NOT_FOUND));
        // 공연장 확인
        Places places = placesRepository.findById(placeId).orElseThrow(() -> new EventErrorException(EVENT_NOT_FOUND));
        // 공연과 공연장 일치 확인
        if (!eventPlaceRepository.existsByEventsAndPlaces(events, places)) {
            throw new EventErrorException(NOT_MATCH);
        }
        // 공연 시간 확인(회차)
        EventTimes eventTime = eventTimesRepository.findById(eventTimeId).orElseThrow(() -> new EventErrorException(EVENT_NOT_FOUND));

        SearchSeatRequestDto request = new SearchSeatRequestDto(
                eventId, placeId, eventTimeId);

        Page<PartRowColSeatResponseDto> responseSeat = eventTimeReserveSeatRepository.searchSeat(
                request, partId, rowId, colId, pageable);

        return responseSeat;
    }

    @Transactional
    public void updateSeat(MultipartFile updateFile, Long placeId, Long eventId) {
        // 좌석 등급 설정할 공연이 맞는지 확인
        Events events = eventsRepository.findById(eventId).orElseThrow(() -> new EventErrorException(EVENT_NOT_FOUND));
        // 좌석 등급 설정할 공연장 확인
        Places places = placesRepository.findById(placeId).orElseThrow(() -> new EventErrorException(EVENT_NOT_FOUND));
        // 공연과 공연장 일치 확인
        if (!eventPlaceRepository.existsByEventsAndPlaces(events, places)) {
            throw new EventErrorException(NOT_MATCH);
        }
        try (Reader reader = new InputStreamReader(updateFile.getInputStream())) {
            // csv 파일을 List <Dto> 형태로 반환
            List<SeatResponseDto> seatList = new CsvToBeanBuilder<SeatResponseDto>(reader)
                    .withType(SeatResponseDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();
            // List를 반복문으로 하나씩 데이터 저장
            eventSeatRepository.updateEventSeatListByEventsIdAndPlaceId(eventId, placeId, seatList);
        } catch (IOException e) {
            throw new EventErrorException(IO_ERROR);
        }
    }

    @Transactional
    public void deleteSeat(MultipartFile seatFile, Long placeId) {
        // 공연장 존재 여부 확인
        Places place = placesRepository.findById(placeId)
                .orElseThrow(() -> new EventErrorException(EVENT_NOT_FOUND));
        // 공연장 기본 좌석 배치도 유무 확인 있어야 삭제 가능
        if (place.getSeatList().isEmpty()) {
            throw new EventErrorException(SEAT_NOT_FOUND);
        }
        try (Reader reader = new InputStreamReader(seatFile.getInputStream())) {
            List<BaseSeatResponseDto> softDeleteSeatList = new CsvToBeanBuilder<BaseSeatResponseDto>(reader)
                    .withType(BaseSeatResponseDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

            seatsRepository.softDeleteSeatByList(softDeleteSeatList);

        } catch (IOException e) {
            throw new EventErrorException(IO_ERROR);
        }

    }
}
