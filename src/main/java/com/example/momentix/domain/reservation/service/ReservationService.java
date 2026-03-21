package com.example.momentix.domain.reservation.service;

import com.example.momentix.domain.common.exception.auth.AuthErrorException;
import com.example.momentix.domain.common.exception.event.EventErrorException;
import com.example.momentix.domain.common.exception.reservation.ReservationErrorException;
import com.example.momentix.domain.events.entity.EventPlace;
import com.example.momentix.domain.events.entity.Events;
import com.example.momentix.domain.events.entity.eventtimes.EventTimeReserveSeat;
import com.example.momentix.domain.events.entity.eventtimes.EventTimes;
import com.example.momentix.domain.events.repository.EventPlaceRepository;
import com.example.momentix.domain.events.repository.EventsRepository;
import com.example.momentix.domain.events.repository.eventtimes.EventTimeReserveSeatRepository;
import com.example.momentix.domain.events.repository.eventtimes.EventTimesRepository;
import com.example.momentix.domain.reservation.dto.ReservationResponseDto;
import com.example.momentix.domain.reservation.entity.ReservationStatusType;
import com.example.momentix.domain.reservation.entity.Reservations;
import com.example.momentix.domain.reservation.repository.RedisLockRepository;
import com.example.momentix.domain.reservation.repository.ReservationRepository;
import com.example.momentix.domain.users.entity.Users;
import com.example.momentix.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

import static com.example.momentix.domain.common.exception.auth.AuthErrorCode.NOT_FOUND;
import static com.example.momentix.domain.common.exception.event.EventErrorCode.*;
import static com.example.momentix.domain.common.exception.reservation.ReservationErrorCode.NO_MY_RESERVATION;
import static com.example.momentix.domain.common.exception.reservation.ReservationErrorCode.NO_RESERVATION;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationsRepository;
    private final UserRepository usersRepository;
    private final EventsRepository eventsRepository;
    private final EventPlaceRepository eventPlaceRepository;
    private final EventTimesRepository eventTimesRepository;
    private final EventTimeReserveSeatRepository eventTimeReserveSeatRepository;
    private final RedisLockRepository redisLockRepository;

    @Transactional
    public ReservationResponseDto selectAll(Long userId, Long eventId, Long eventPlaceId, Long eventTimeId) {

        //이용자와 공연 존재 확인
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));

        Events event = eventsRepository.findById(eventId)
                .orElseThrow(() -> new EventErrorException(EVENT_NOT_FOUND));

        //해당 상태의 예매 상태가 있는지 조회
        Reservations reservation = reservationsRepository.findActiveByUsers_UsersIdAndEvents_Id(
                userId, eventId, List.of(
                        ReservationStatusType.DRAFT,
                        ReservationStatusType.SELECT_PLACE,
                        ReservationStatusType.SELECT_TIME,
                        ReservationStatusType.SELECT_SEAT,
                        ReservationStatusType.WAIT_PAYMENT
                )).orElseGet(() -> Reservations.builder()
                .users(user)
                .events(event)
                .reservationStatusType(ReservationStatusType.DRAFT)
                .build());

        //해당 공연이 공연 장소와 일치하는지
        EventPlace eventPlace = eventPlaceRepository.findByIdAndEventsId(eventPlaceId, eventId).orElseThrow(
                () -> new EventErrorException(NOT_EVENT));

        reservation.selectEventPlace(eventPlace);
        EventTimes eventTimes = eventTimesRepository.findByIdAndEventsId(eventTimeId, eventId).orElseThrow(
                () -> new EventErrorException(NOT_SELECT_TIME));

        reservation.selectEventTime(eventTimes);

        reservationsRepository.save(reservation);

        return ReservationResponseDto.from(reservation);
    }

    //공연 선택
    @Transactional
    public ReservationResponseDto selectEvent(Long userId, Long eventId) {
        //이용자와 공연 존재 확인
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));

        Events event = eventsRepository.findById(eventId)
                .orElseThrow(() -> new EventErrorException(EVENT_NOT_FOUND));

        //해당 상태의 예매 상태가 있는지 조회
        Reservations reservation = reservationsRepository.findActiveByUsers_UsersIdAndEvents_Id(
                userId, eventId, List.of(
                        ReservationStatusType.DRAFT,
                        ReservationStatusType.SELECT_PLACE,
                        ReservationStatusType.SELECT_TIME,
                        ReservationStatusType.SELECT_SEAT,
                        ReservationStatusType.WAIT_PAYMENT
                )).orElseGet(() -> Reservations.builder()
                .users(user)
                .events(event)
                .reservationStatusType(ReservationStatusType.DRAFT)
                .build()
        );

        reservationsRepository.save(reservation);

        return ReservationResponseDto.from(reservation);
    }

    // 장소 재선택 - 따로 뺀 이유 : 재선택 하려면 어느 예약을 바꿀지 reservationId가 필요
    @Transactional
    public ReservationResponseDto reselectEvent(
            Long userId,
            Long reservationId,
            Long eventId
    ) {
        if (!usersRepository.existsById(userId)) {
            throw new AuthErrorException(NOT_FOUND);
        }
        Reservations reservations = reservationsRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationErrorException(NO_RESERVATION));
        if (!reservations.getUsers().getUserId().equals(userId)) {
            throw new ReservationErrorException(NO_MY_RESERVATION);
        }

        // 허용 상태만 재선택 가능 (최소한의 체크)
        switch (reservations.getReservationStatusType()) {
            case DRAFT, SELECT_PLACE, SELECT_TIME, SELECT_SEAT -> {
            }
            default -> throw new IllegalArgumentException("공연 선택이 불가능합니다.");
        }

        Events event = eventsRepository.findById(eventId)
                .orElseThrow(() -> new EventErrorException(EVENT_NOT_FOUND));

        // 최소 변경만 적용 (엔티티에 전용 메서드가 있으면 그걸 사용)
        reservations.setEvents(event);

        return ReservationResponseDto.from(reservations);
    }

    //공연 장소 선택하기
    @Transactional
    public ReservationResponseDto selectEventPlace(Long userId, Long reservationId, Long eventPlaceId) {
        //사용자 확인
        if (!usersRepository.existsById(userId)) {
            throw new AuthErrorException(NOT_FOUND);
        }
        //해당 예매 아이디 확인
        Reservations reservations = reservationsRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationErrorException(NO_RESERVATION));

        //예매 대기가 본인이 아닐경우
        if (!reservations.getUsers().getUserId().equals(userId)) {
            throw new ReservationErrorException(NO_MY_RESERVATION);
        }

        //공연을 선택하지 않았을 경우
        if (reservations.getEvents() == null) {
            throw new EventErrorException(FIRST_SELECT_EVENT);
        }
        //상태 체크: 장소 재선택 허용 범위 확대
        // (최초 선택, 시간 선택 중, 좌석 선택 중 모두 허용)
        switch (reservations.getReservationStatusType()) {

            case DRAFT, SELECT_PLACE, SELECT_TIME, SELECT_SEAT -> {
            }

            default -> throw new EventErrorException(EVENT_SELECTION_NOT_AVAILABLE);
        }

        //공연 아이디를 가져와서
        Long eventsId = reservations.getEvents().getId();

        //해당 공연이 공연 장소와 일치하는지
        EventPlace eventPlace = eventPlaceRepository.findByIdAndEventsId(eventPlaceId, eventsId).orElseThrow(
                () -> new EventErrorException(NOT_EVENT));

        reservations.selectEventPlace(eventPlace);

        return ReservationResponseDto.from(reservations);
    }

    @Transactional
    public ReservationResponseDto selectEventTime(Long userId, Long reservationId, Long eventTimeId) {
        if (!usersRepository.existsById(userId)) {
            throw new AuthErrorException(NOT_FOUND);
        }

        Reservations reservations = reservationsRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationErrorException(NO_RESERVATION));

        if (!reservations.getUsers().getUserId().equals(userId)) {
            throw new ReservationErrorException(NO_MY_RESERVATION);
        }

        if (reservations.getEvents() == null) {
            throw new EventErrorException(FIRST_SELECT_EVENT);
        }
        //공연 장소 선택 확인
        if (reservations.getEventPlace() == null) {
            throw new EventErrorException(NOT_EVENT_LOCAL);
        }

        //해당 상태에는 시간 선택이 불가능 -> 추후 상태 관리를 정확히 명시하여, if(==null) 로 처리하던 부분 상태로 변경
        switch (reservations.getReservationStatusType()) {
            //최초 선택(SELECT_PLACE) 허용, 시간 재선택(SELECT_TIME), 좌석 선택 이후 재선택(SELECT_SEAT)도 허용
            case SELECT_PLACE, SELECT_TIME, SELECT_SEAT -> {
            }
            default -> throw new EventErrorException(NOT_SELECT_TIME);
        }

        Long eventsId = reservations.getEvents().getId();
        EventTimes eventTimes = eventTimesRepository.findByIdAndEventsId(eventTimeId, eventsId).orElseThrow(
                () -> new EventErrorException(NOT_SELECT_TIME));

        reservations.selectEventTime(eventTimes);

        return ReservationResponseDto.from(reservations);

    }

    //Reservation을 임시 테이블 처럼 사용하기 때문에, 티켓(예매 내역)이 생성되는 순간 제거 -> CreateTicket 에 RS.deleteReservation 추가

    //Propagation.REQUIRED 가 default 지만, 티켓 생성 트랜잭션에 사용할 예정이라 명시
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteReservation(Long userId, Long reservationId) {
        if (!usersRepository.existsById(userId)) {
            throw new AuthErrorException(NOT_FOUND);
        }

        Reservations reservations = reservationsRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationErrorException(NO_RESERVATION));

        if (!reservations.getUsers().getUserId().equals(userId)) {
            throw new ReservationErrorException(NO_RESERVATION);
        }
        reservationsRepository.deleteById(reservationId);

    }

    // 좌석 선택 (좌석 상태: AVAILABLE -> HOLD)
    @Transactional
    public ReservationResponseDto selectEventSeat(Long userId, Long reservationId, Long eventTimeReserveSeatId) {

        // --- 1. 기존 유효성 검증 로직 ---
        if (!usersRepository.existsById(userId)) {
            throw new AuthErrorException(NOT_FOUND);
        }
        Reservations r = reservationsRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationErrorException(NO_RESERVATION));
        if (!r.getUsers().getUserId().equals(userId)) {
            throw new ReservationErrorException(NO_MY_RESERVATION);
        }
        if (r.getEvents() == null)
            throw new EventErrorException(FIRST_SELECT_EVENT);
        if (r.getEventPlace() == null)
            throw new EventErrorException(NOT_EVENT_LOCAL);
        if (r.getEventTimes() == null)
            throw new EventErrorException(NOT_SELECT_TIME);
        switch (r.getReservationStatusType()) {
            case SELECT_TIME, SELECT_SEAT -> {
            }
            default -> throw new EventErrorException(SEAT_NOT_FOUND);
        }

        // --- 2. Lua 스크립트 + DB 낙관적 락 적용 ---
        String lockKey = "seat_lock:" + eventTimeReserveSeatId;

        // 1차 잠금: Lua 스크립트 시도 (로직이 매우 간결해짐)
        if (!redisLockRepository.lock(lockKey, Duration.ofMinutes(5))) {
            throw new EventErrorException(SEAT_ALREADY_BOOKED);
        }

        try {
            // 2차 잠금: DB 낙관적 락으로 좌석 조회 및 상태 변경
            EventTimeReserveSeat seat = eventTimeReserveSeatRepository.findById(eventTimeReserveSeatId)
                    .orElseThrow(() -> new EventErrorException(SEAT_NOT_FOUND));

            seat.hold();
            r.selectEventSeat(seat);

        } catch (ObjectOptimisticLockingFailureException e) {
            throw new EventErrorException(SEAT_ALREADY_BOOKED);
        } finally {
            // 3. 작업 완료 후 안전하게 Redis 락 해제
            redisLockRepository.unlock(lockKey);
        }

        return ReservationResponseDto.from(r);
    }
}
