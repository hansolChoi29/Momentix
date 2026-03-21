package com.example.momentix.domain.common.exception.review;


public enum ReviewCode {

    INVALID_RATING_UNIT(400, "별점은 0.5 단위로만 입력할 수 있습니다."),
    FORBIDDEN(403, "리뷰를 수정할 권한이 없습니다."),
    NO_PERMISSION_TO_MODIFY(403, "리뷰를 수정하거나 삭제할 권한이 없습니다."),
    WRITE_NOT_PERMITTED(403, "리뷰를 작성할 권한이 없습니다. 공연을 예매한 사용자만 작성할 수 있습니다."),
    REVIEW_NOT_FOUND(404, "해당 리뷰를 찾을 수 없습니다."),
    ALREADY_REVIEWED(409, "이미 해당 공연에 대한 리뷰를 작성했습니다."),
    REVIEW_NOT_PURCHASED(403, "해당 공연을 예매한 사용자만 리뷰를 작성할 수 있습니다."),
    REVIEW_NOT_AUTHORIZED(403, "리뷰를 삭제할 권한이 없습니다.");

    private final int status;
    private final String message;

    ReviewCode(int status, String message){
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
