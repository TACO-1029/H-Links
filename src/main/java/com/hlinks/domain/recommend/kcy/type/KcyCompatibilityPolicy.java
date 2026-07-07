package com.hlinks.domain.recommend.kcy.type;

import java.util.EnumMap;
import java.util.Map;

public final class KcyCompatibilityPolicy {

    private static final KcyType[] TYPE_ORDER = KcyType.values();
    private static final Map<KcyType, Map<KcyType, KcyMatchGrade>> COMPATIBILITY_TABLE = buildCompatibilityTable();

    private KcyCompatibilityPolicy() {
    }

    public static KcyMatchGrade gradeOf(KcyType myType, KcyType partnerType) {
        return COMPATIBILITY_TABLE
                .getOrDefault(myType, Map.of())
                .getOrDefault(partnerType, KcyMatchGrade.B);
    }

    public static String reasonOf(KcyType myType, KcyType partnerType, KcyMatchGrade grade) {
        StringBuilder reason = new StringBuilder();
        reason.append(grade.getLabel()).append("입니다. ");

        if (myType.name().charAt(0) != partnerType.name().charAt(0)) {
            reason.append("실행과 설계 관점을 서로 보완할 수 있고, ");
        }

        if (myType.name().charAt(1) != partnerType.name().charAt(1)) {
            reason.append("넓게 탐색하는 시야와 깊게 파고드는 분석력을 함께 가져갈 수 있습니다. ");
        }

        if (myType.name().charAt(2) != partnerType.name().charAt(2)) {
            reason.append("개인 몰입과 협업 조율 방식이 달라 스터디 진행에 균형을 만들 수 있습니다. ");
        }

        if (myType.name().charAt(3) != partnerType.name().charAt(3)) {
            reason.append("AI 도구 활용과 직접 구현 중심의 접근을 함께 비교하며 성장하기 좋습니다. ");
        }

        if (reason.toString().equals(grade.getLabel() + "입니다. ")) {
            reason.append("비슷한 개발 리듬을 바탕으로 편안하게 관점을 나누기 좋은 조합입니다.");
        }

        return reason.toString().trim();
    }

    private static Map<KcyType, Map<KcyType, KcyMatchGrade>> buildCompatibilityTable() {
        String[] gradeRows = {
                "BBACABSBAASASSSS",
                "BBCABABSAAASSSSS",
                "ACBBSBABSAAASSSS",
                "CABBBSBAASAAASSS",
                "ABSBBBACSSSSAASA",
                "BABSBBCASSSSAAAS",
                "SBABACBBSSSSSAAA",
                "BSBACABBSSSSASAA",
                "AASASSSSBBACABSB",
                "AAASSSSSBBCABABS",
                "SAAASSSSACBBSBAB",
                "ASAASSSSCABBBSBA",
                "SSSAAASAABSBBBAC",
                "SSSSAAASBABSBBCA",
                "SSSSSAAASBABACBB",
                "SSSSASAABSBACABB"
        };

        Map<KcyType, Map<KcyType, KcyMatchGrade>> table = new EnumMap<>(KcyType.class);

        for (int rowIndex = 0; rowIndex < TYPE_ORDER.length; rowIndex++) {
            Map<KcyType, KcyMatchGrade> row = new EnumMap<>(KcyType.class);
            String grades = gradeRows[rowIndex];
            validateGradeRow(rowIndex, grades);

            for (int columnIndex = 0; columnIndex < TYPE_ORDER.length; columnIndex++) {
                row.put(TYPE_ORDER[columnIndex], KcyMatchGrade.from(grades.charAt(columnIndex)));
            }

            table.put(TYPE_ORDER[rowIndex], row);
        }

        validateSymmetric(table);

        return table;
    }

    private static void validateGradeRow(int rowIndex, String grades) {
        if (grades.length() != TYPE_ORDER.length) {
            throw new IllegalStateException("KCY compatibility row length is invalid. rowIndex=" + rowIndex);
        }
    }

    private static void validateSymmetric(Map<KcyType, Map<KcyType, KcyMatchGrade>> table) {
        for (KcyType myType : TYPE_ORDER) {
            for (KcyType partnerType : TYPE_ORDER) {
                KcyMatchGrade forwardGrade = table.get(myType).get(partnerType);
                KcyMatchGrade reverseGrade = table.get(partnerType).get(myType);

                if (forwardGrade != reverseGrade) {
                    throw new IllegalStateException(
                            "KCY compatibility table must be symmetric. myType="
                                    + myType + ", partnerType=" + partnerType
                    );
                }
            }
        }
    }
}
