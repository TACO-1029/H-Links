package com.hlinks.domain.recommend.kcy.exception;

import com.hlinks.global.response.code.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum KcyErrorCode implements BaseResponseCode {

    KCY_QUESTION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "KCY_QUESTION_NOT_FOUND",
            "테스트 질문을 찾을 수 없습니다"
    ),
    KCY_OPTION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "KCY_OPTION_NOT_FOUND",
            "테스트 문항을 찾을 수 없습니다"
    ),
    KCY_ANSWER_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "KCY_ANSWER_REQUIRED",
                    "모든 문항에 응답해주세요."
    ),
    KCY_INVALID_ANSWER_COUNT(
            HttpStatus.BAD_REQUEST,
            "KCY_INVALID_ANSWER_COUNT",
                    "KCY 테스트는 모든 문항에 하나씩 응답해야 합니다."
    ),
    KCY_INVALID_ANSWER_DUPLICATED(
            HttpStatus.BAD_REQUEST,
            "KCY_INVALID_ANSWER_DUPLICATED",
                    "한 문항에는 하나의 선택지만 선택할 수 있습니다."
    ),
    KCY_SCORE_CALCULATION_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "KCY_SCORE_CALCULATION_FAILED",
                    "KCY 결과를 계산하는 중 문제가 발생했습니다."
    ),
    KCY_RESULT_SAVE_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "KCY_RESULT_SAVE_FAILED",
                    "KCY 결과를 저장하는 중 문제가 발생했습니다."
    ),
    KCY_RESULT_TYPE_NOT_FOUND(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "KCY_RESULT_TYPE_NOT_FOUND",
                    "KCY 결과 유형을 찾을 수 없습니다."
    ),
    KCY_INVALID_INPUT_VALUE(
            HttpStatus.BAD_REQUEST,
            "KCY_INVALID_INPUT_VALUE",
            "잘못된 입력값 형식입니다."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
