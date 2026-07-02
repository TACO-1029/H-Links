package com.hlinks.domain.quiz.ai;

public class AiQuizException extends RuntimeException {

    public AiQuizException(String message) {
        super(message);
    }

    public AiQuizException(String message, Throwable cause) {
        super(message, cause);
    }
}
