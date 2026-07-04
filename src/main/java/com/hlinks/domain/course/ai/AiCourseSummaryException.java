package com.hlinks.domain.course.ai;

import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;

public class AiCourseSummaryException extends BaseException {

    public AiCourseSummaryException(String message) {
        super(ErrorResponseCode.INTERNAL_SERVER_ERROR, message);
    }

    public AiCourseSummaryException(String message, Throwable cause) {
        super(ErrorResponseCode.INTERNAL_SERVER_ERROR, message, cause);
    }
}
