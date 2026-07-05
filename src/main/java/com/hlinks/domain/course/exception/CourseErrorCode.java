package com.hlinks.domain.course.exception;

import com.hlinks.global.response.code.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CourseErrorCode implements BaseResponseCode {

    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_404_1", "존재하지 않는 강의입니다."),
    COURSE_NOT_OPEN(HttpStatus.BAD_REQUEST, "COURSE_400_1", "신청할 수 없는 강의 상태입니다."),
    COURSE_APPLY_PERIOD_NOT_OPEN(HttpStatus.BAD_REQUEST, "COURSE_400_2", "강의 신청 기간이 아닙니다."),
    COURSE_CAPACITY_FULL(HttpStatus.BAD_REQUEST, "COURSE_400_3", "강의 정원이 마감되었습니다."),
    COURSE_QUIZ_NOT_READY(HttpStatus.BAD_REQUEST, "COURSE_400_4", "퀴즈 생성이 완료된 후 수강신청할 수 있습니다."),
    COURSE_ALREADY_APPLIED(HttpStatus.CONFLICT, "COURSE_409_1", "이미 신청한 강의입니다."),
    COURSE_APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_404_2", "취소할 수 있는 강의 신청 내역이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
