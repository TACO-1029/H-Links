package com.hlinks.domain.quiz.ai;

import org.springframework.stereotype.Component;

@Component
public class AiQuizPromptBuilder {

    public String build(String sourceText, int quizCount) {
        return """
                다음 강의 내용을 기반으로 객관식 퀴즈를 생성하세요.

                요구사항:
                - 반드시 JSON만 반환하세요.
                - JSON 외의 설명 문장, 마크다운 코드블록, ```json 표기를 절대 포함하지 마세요.
                - quizzes 배열에 %d개의 퀴즈를 생성하세요.
                - questionType은 반드시 "MULTIPLE_CHOICE"로 작성하세요.
                - difficulty는 EASY, MEDIUM, HARD 중 하나로 작성하세요.
                - options는 반드시 4개 생성하세요.
                - 정답은 correctYn이 "Y"인 선택지 하나만 존재해야 합니다.
                - 나머지 선택지는 correctYn을 "N"으로 작성하세요.
                - optionNo는 1, 2, 3, 4 숫자로 작성하세요.
                - explanation에는 정답 해설을 작성하세요.

                반환 JSON 형식:
                {
                  "quizzes": [
                    {
                      "questionType": "MULTIPLE_CHOICE",
                      "questionText": "문제 내용",
                      "difficulty": "MEDIUM",
                      "explanation": "정답 해설",
                      "answerText": "정답 텍스트",
                      "options": [
                        {
                          "optionNo": 1,
                          "optionText": "선택지 내용",
                          "correctYn": "N"
                        },
                        {
                          "optionNo": 2,
                          "optionText": "선택지 내용",
                          "correctYn": "Y"
                        },
                        {
                          "optionNo": 3,
                          "optionText": "선택지 내용",
                          "correctYn": "N"
                        },
                        {
                          "optionNo": 4,
                          "optionText": "선택지 내용",
                          "correctYn": "N"
                        }
                      ]
                    }
                  ]
                }

                강의 내용:
                %s
                """.formatted(quizCount, sourceText);
    }
}