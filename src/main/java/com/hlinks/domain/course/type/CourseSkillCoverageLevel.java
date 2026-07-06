package com.hlinks.domain.course.type;

import java.util.Locale;

public enum CourseSkillCoverageLevel {
    BASIC(1, "기초 개념과 입문 수준으로 다루는 강의입니다."),
    INTERMEDIATE(2, "실습과 활용 중심으로 다루는 강의입니다."),
    ADVANCED(3, "내부 원리와 심화 주제까지 다루는 강의입니다.");

    private final int rank;
    private final String defaultReason;

    CourseSkillCoverageLevel(int rank, String defaultReason) {
        this.rank = rank;
        this.defaultReason = defaultReason;
    }

    public int getRank() {
        return rank;
    }

    public String getDefaultReason() {
        return defaultReason;
    }

    public static CourseSkillCoverageLevel from(String value) {
        if (value == null || value.isBlank()) {
            return BASIC;
        }

        try {
            return CourseSkillCoverageLevel.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return BASIC;
        }
    }
}
