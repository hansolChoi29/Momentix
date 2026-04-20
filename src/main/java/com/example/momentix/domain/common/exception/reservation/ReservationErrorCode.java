package com.example.momentix.domain.common.exception.reservation;


public enum ReservationErrorCode {

    NO_RESERVATION(404,"존재하지 않는 예약입니다."),
    NO_MY_RESERVATION(403,"본인 예약이 아닙니다."),
    ALREADY_TICKET(404,"이미 이 예약으로 발급된 티켓이 있습니다."),
    PENDING_PAYMENT(404,"이미 대기 중(PENDING)인 결제가 있습니다."),
    AGE_RESTRICTED(409,"관람 연령 제한으로 예매가 불가합니다.");


    private final int status;
    private final String message;

    ReservationErrorCode(int status, String message){
        this.status=status;
        this.message=message;
    }

    public int getStatus(){
        return status;
    }
    public String getMessage(){
        return message;
    }
}


// throw new IllegalArgumentException("시간 선택이 불가능합니다.");
//IllegalArgumentException("존재하지 않는 사용자입니다.");
//        throw new IllegalStateException("이미 다른 사용자가 선점 중인 좌석입니다.");
//IllegalStateException("이미 선점(HOLD)되었거나 선택 불가한 좌석입니다.");
//throw new IllegalStateException("이미 대기 중(PENDING)인 결제가 있습니다.");


