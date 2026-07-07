package com.hlinks.domain.recommend.kcy.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KcyMatchGrade {

    S(100, "찰떡 보완 페어"),
    A(80, "좋은 자극 페어"),
    B(60, "편안한 공감 페어"),
    C(40, "주제 명확할 때 좋은 페어");

    private final int score;
    private final String label;

    public static KcyMatchGrade from(char grade) {
        return switch (Character.toUpperCase(grade)) {
            case 'S' -> S;
            case 'A' -> A;
            case 'B' -> B;
            case 'C' -> C;
            default -> B;
        };
    }
}
