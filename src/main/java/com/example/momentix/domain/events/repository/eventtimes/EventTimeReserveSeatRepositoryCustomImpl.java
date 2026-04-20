package com.example.momentix.domain.events.repository.eventtimes;

import com.example.momentix.domain.events.dto.request.SearchSeatRequestDto;
import com.example.momentix.domain.events.dto.response.PartRowColSeatResponseDto;
import com.example.momentix.domain.events.entity.QEventSeat;
import com.example.momentix.domain.events.entity.enums.SeatPartType;
import com.example.momentix.domain.events.entity.eventtimes.QEventTimeReserveSeat;
import com.example.momentix.domain.events.entity.eventtimes.QEventTimes;
import com.example.momentix.domain.events.entity.seats.QSeats;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public class EventTimeReserveSeatRepositoryCustomImpl implements EventTimeReserveSeatRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public EventTimeReserveSeatRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    private BooleanExpression partEq(Long partId) {
        if (partId == null) {
            return null;
        }

        SeatPartType seatPartType = SeatPartType.fromId(partId);
        return seatPartType != null ? QEventSeat.eventSeat.seatPartType.eq(seatPartType) : null;
    }

    private BooleanExpression rowEq(Long rowId) {
        return rowId != null ? QSeats.seats.seatRow.eq(rowId) : null;
    }

    private BooleanExpression colEq(Long colId) {
        return colId != null ? QSeats.seats.seatCol.eq(colId) : null;
    }

    @Override
    public Page<PartRowColSeatResponseDto> searchSeat(
            SearchSeatRequestDto requestDto,
            Long partId,
            Long rowId,
            Long colId,
            Pageable pageable) {

//        System.out.println("eventId = " + requestDto.getEventId());
//        System.out.println("placeId = " + requestDto.getPlaceId());
//        System.out.println("eventTimeId = " + requestDto.getEventTimeId());
//        System.out.println("partId = " + partId);
//        System.out.println("rowId = " + rowId);
//        System.out.println("colId = " + colId);

        QSeats s = QSeats.seats;
        QEventTimeReserveSeat etrs = QEventTimeReserveSeat.eventTimeReserveSeat;
        QEventSeat es = QEventSeat.eventSeat;
        QEventTimes et = QEventTimes.eventTimes;


        List<PartRowColSeatResponseDto> seatResponse = queryFactory
                .select(Projections.constructor(PartRowColSeatResponseDto.class,
                        s.id,
                        es.seatGradeType,
                        es.seatPartType,
                        es.seatPrice,
                        etrs.seatReserveStatus,
                        s.seatRow,
                        s.seatCol
                ))
                .from(etrs)
                .join(etrs.eventSeat, es)
                .join(es.seats, s)
                .join(etrs.eventTimes, et)
                .where(
                        etrs.eventTimes.id.eq(requestDto.getEventTimeId()),
                        et.events.id.eq(requestDto.getEventId()),
                        es.events.id.eq(requestDto.getEventId()),
                        s.places.id.eq(requestDto.getPlaceId()),
                        es.seats.id.eq(s.id),
                        partEq(partId),
                        rowEq(rowId),
                        colEq(colId)
                )
                .offset(pageable.getOffset())   // (2) 페이지 번호
                .limit(pageable.getPageSize())  // (3) 페이지 사이즈
                .fetch();
        long total = Optional.ofNullable(queryFactory
                .select(etrs.count())
                .from(etrs)
                .join(etrs.eventSeat, es)
                .join(es.seats, s)
                .join(etrs.eventTimes, et)
                .where(
                        etrs.eventTimes.id.eq(requestDto.getEventTimeId()),
                        et.events.id.eq(requestDto.getEventId()),
                        es.events.id.eq(requestDto.getEventId()),
                        s.places.id.eq(requestDto.getPlaceId()),
                        es.seats.id.eq(s.id),
                        partEq(partId),
                        rowEq(rowId),
                        colEq(colId)
                )
                .fetchOne()).orElse(0L);

        return new PageImpl<>(seatResponse, pageable, total);
    }
}
