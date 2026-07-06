package com.hlinks.domain.course.ai;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourseSummaryPromptBuilder {

    public String build(String transcriptText, List<String> availableSkillNames) {
        if (!StringUtils.hasText(transcriptText)) {
            throw new AiCourseSummaryException("강의 요약을 위한 transcriptText는 필수입니다.");
        }

        String availableSkills = formatAvailableSkills(availableSkillNames);

        return """
                아래 강의 transcript를 분석하여 강의 메타데이터로 활용 가능한 요약을 생성하세요.

                요구사항:
                - 반드시 유효한 JSON만 반환하세요.
                - JSON 외 설명이나 markdown code block은 반환하지 마세요.
                - 원문에 없는 내용은 추측하지 마세요.
                - summaryText에는 핵심 개념, 주요 용어, 절차, 예시, 난이도 단서가 포함되어야 합니다.
                - summaryText는 퀴즈 생성뿐 아니라 레벨테스트 기반 강의 추천, 커리어패스 추천, 강의 태그/스킬 매핑에도 사용할 수 있어야 합니다.
                - 아래 사용 가능한 skillName 목록 중 의미상 유사한 항목이 있으면 skills.skillName에는 반드시 기존 skillName을 그대로 작성하세요.
                - 기존 skillName에 매핑한 경우 skills.newSkillYn은 "N"으로 작성하세요.
                - 기존 skillName 중 의미상 유사한 항목이 없으면 추천/분류에 사용할 수 있는 짧은 신규 skillName을 작성하세요.
                - 신규 skillName을 작성한 경우 skills.newSkillYn은 "Y"로 작성하세요.
                - skills.sourceSkillName에는 강의 내용에서 발견한 실제 세부 표현을 작성하세요.
                - 관련 스킬이 없으면 skills는 빈 배열로 반환하세요.
                - skills.weight는 챕터에서 해당 skill이 차지하는 학습 비중을 0~1 사이 소수로 작성하세요.
                - skills.weight의 합은 반드시 1.0이 되도록 작성하세요.
                - 핵심 학습 목표이거나 실습의 중심 기술은 0.6~0.8 수준으로 크게 부여하세요.
                - 핵심 기술을 이해하기 위한 주요 보조 기술은 0.2~0.4 수준으로 부여하세요.
                - 간단히 언급되거나 환경/배경으로만 사용되는 기술은 0.05~0.15 수준으로 작게 부여하세요.
                - 단순 언급 빈도보다 학습 목표 중심성을 우선하여 weight를 산정하세요.
                - skills.coverageLevel은 해당 skill을 어느 깊이로 다루는지 BASIC, INTERMEDIATE, ADVANCED 중 하나로 작성하세요.
                - BASIC은 환경 설정, 기본 개념, 입문 문법, 기초 용어 중심입니다.
                - INTERMEDIATE는 실습, 활용, 프로젝트 적용, API 사용, 실무 흐름 중심입니다.
                - ADVANCED는 내부 동작 원리, 성능 최적화, 아키텍처, 트러블슈팅 중심입니다.
                - skills.coverageReason에는 coverageLevel로 판단한 근거를 한 문장으로 작성하세요.
                - weight는 학습 비중이고 coverageLevel은 학습 깊이입니다. 두 값을 서로 독립적으로 판단하세요.

                사용 가능한 skillName 목록:
                %s

                JSON 형식:
                {
                  "summaryText": "강의 핵심 내용을 자연어로 요약한 텍스트",
                  "skills": [
                    {
                      "skillName": "최종 저장할 스킬명",
                      "sourceSkillName": "강의 내용에서 발견한 실제 세부 표현",
                      "newSkillYn": "N",
                      "weight": 0.7,
                      "coverageLevel": "INTERMEDIATE",
                      "coverageReason": "프로젝트 적용과 API 활용 흐름을 중심으로 설명합니다."
                    }
                  ]
                }

                transcript:
                %s
                """.formatted(availableSkills, transcriptText.trim());
    }

    private String formatAvailableSkills(List<String> availableSkillNames) {
        if (availableSkillNames == null || availableSkillNames.isEmpty()) {
            return "- 사용 가능한 skillName 없음";
        }

        return availableSkillNames.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .map(skillName -> "- " + skillName)
                .collect(Collectors.joining("\n"));
    }
}
