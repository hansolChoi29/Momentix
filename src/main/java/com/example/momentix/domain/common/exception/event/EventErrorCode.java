package com.example.momentix.domain.common.exception.event;


public enum EventErrorCode {
    SEAT_SELECTION(400, "선택한 좌석 정보가 올바르지 않습니다."),
    FORBIDDEN(403, "해당 공연을 관리할 권한이 없습니다."),
    EVENT_NOT_FOUND(404, "존재하지 않는 공연입니다."),
    SEAT_NOT_FOUND(404, "존재하지 않는 좌석입니다."),
    EVENT_ALREADY_EXISTS(409, "이미 등록된 공연 정보입니다."),
    SEAT_ALREADY_BOOKED(409, "이미 예매된 좌석입니다."),
    SOLD_OUT(409, "모든 좌석이 매진되었습니다."),
    PURCHASE_LIMIT_EXCEEDED(409, "예매 가능한 수량을 초과했습니다."),
    EVENT_NOT_OPEN_FOR_BOOKING(409, "아직 예매가 시작되지 않은 공연입니다."),
    SALES_PERIOD_ENDED(409, "예매 기간이 종료된 공연입니다."),
    EVENT_HAS_ENDED(409, "이미 종료된 공연입니다."),
    EVENT_CANCELLED(409, "취소된 공연입니다."),
    NOT_MATCH(404, "공연과 공연장이 일치하지 않음"),
    IO_ERROR(500, "입출력 처리 중 오류가 발생했습니다."),
    DM_OPEN_FAILED(400, "DM 오픈 실패"),
    MESSAGE_SEND_FAILED(400, "메시지 전송 실패"),
    FIRST_SELECT_EVENT(404, "공연이 먼저 선택되어야 합니다."),
    NOT_EVENT_LOCAL(404, "공연 장소가 선택되지 않았습니다."),
    NOT_TIME(404, "해당 공연에 공연 시간이 없습니다."),
    NOT_EVENT(404, "해당 공연의 공연 장소가 없습니다."),
    NOT_SELECT_TIME(404, "공연 시간이 선택되지 않았습니다."),
    EVENT_SELECTION_NOT_AVAILABLE(400, "공연장 선택이 불가능합니다."),
    NO_SLACK(500, "슬랙 메시지 전송 실패");


    private final int status;
    private final String message;

    EventErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
