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
                - skills.weight는 강의에서 해당 skill이 차지하는 중요도를 1~5 사이 정수로 작성하세요.

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
                      "weight": 3
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
