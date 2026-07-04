package com.hlinks.domain.course.ai;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CourseSummaryPromptBuilder {

    public String build(String transcriptText) {
        if (!StringUtils.hasText(transcriptText)) {
            throw new AiCourseSummaryException("강의 요약을 위한 transcriptText는 필수입니다.");
        }

        return """
                아래 강의 transcript를 분석하여 강의 메타데이터로 활용 가능한 요약을 생성하세요.

                요구사항:
                - 반드시 유효한 JSON만 반환하세요.
                - 원문에 없는 내용은 추측하지 마세요.
                - summaryText에는 핵심 개념, 주요 용어, 절차, 예시, 난이도 단서가 포함되어야 합니다.
                - summaryText는 퀴즈 생성뿐 아니라 레벨테스트 기반 강의 추천, 커리어패스 추천, 강의 태그/스킬 매핑에도 사용할 수 있어야 합니다.
                - skills는 강의 추천과 스킬 매핑에 활용 가능한 기술/역량 후보 배열로 작성하세요.
                - skills.skillName은 transcript에서 확인 가능한 기술명, 도구명, 개념명만 작성하세요.
                - skills.weight는 강의에서 해당 skill이 차지하는 중요도를 1~5 사이 정수로 작성하세요.

                JSON 형식:
                {
                  "summaryText": "강의 핵심 내용을 자연어로 요약한 텍스트",
                  "skills": [
                    {
                      "skillName": "기술명",
                      "weight": 3
                    }
                  ]
                }

                transcript:
                %s
                """.formatted(transcriptText.trim());
    }
}
