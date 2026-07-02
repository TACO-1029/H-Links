package com.hlinks.domain.quiz.ai;

import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;

public class AiQuizException extends BaseException {

    public AiQuizException(String message) {
        super(ErrorResponseCode.INTERNAL_SERVER_ERROR, message);
    }

    public AiQuizException(String message, Throwable cause) {
        super(ErrorResponseCode.INTERNAL_SERVER_ERROR, message, cause);
    }
}
