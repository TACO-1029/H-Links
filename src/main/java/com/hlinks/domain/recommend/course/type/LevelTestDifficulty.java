package com.hlinks.domain.recommend.course.type;

import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;

import java.util.Locale;

public enum LevelTestDifficulty {
    LOW,
    MEDIUM,
    HIGH;

    public static LevelTestDifficulty from(String value) {
        if (value == null || value.isBlank()) {
            throw new BaseException(ErrorResponseCode.BAD_REQUEST, "선택 난이도는 필수입니다.");
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case "LOW", "BASIC", "하" -> LOW;
            case "MEDIUM", "INTERMEDIATE", "MID", "중" -> MEDIUM;
            case "HIGH", "ADVANCED", "상" -> HIGH;
            default -> throw new BaseException(
                    ErrorResponseCode.BAD_REQUEST,
                    "지원하지 않는 선택 난이도입니다. selectedDifficulty=" + value
            );
        };
    }
}
