package com.hlinks.domain.quiz.ai;

import org.springframework.stereotype.Component;

@Component
public class QuizPromptBuilder {

    public String build(String sourceText, int quizCount, String difficulty) {
        return """
                아래 강의 내용을 기반으로 학습용 객관식 퀴즈를 생성하세요.

                [역할]
                당신은 HRD 학습 플랫폼에서 강의 내용을 바탕으로 학습자의 이해도를 점검하는 객관식 퀴즈 생성기입니다.

                [생성 규칙]
                - 반드시 제공된 강의 내용에 근거해서만 퀴즈를 생성하세요.
                - 강의 내용에 없는 외부 지식, 추측, 과장된 설명은 절대 포함하지 마세요.
                - quizzes 배열에는 정확히 %d개의 퀴즈를 생성하세요.
                - 모든 퀴즈의 questionType은 반드시 "MULTIPLE_CHOICE"로 작성하세요.
                - 모든 퀴즈의 difficulty는 반드시 "%s"로 작성하세요.
                - 각 퀴즈의 options는 반드시 4개여야 합니다.
                - 각 퀴즈의 정답 선택지는 correctYn이 "Y"인 항목 정확히 1개만 존재해야 합니다.
                - 나머지 선택지는 correctYn을 반드시 "N"으로 작성하세요.
                - optionNo는 반드시 1, 2, 3, 4 숫자를 순서대로 사용하세요.
                - answerText에는 정답 선택지의 핵심 내용을 작성하세요.
                - quiz explanation에는 문제 전체에 대한 핵심 해설을 작성하세요.
                - option explanation에는 해당 선택지가 왜 정답 또는 오답인지 설명하세요.

                [문제 품질 기준]
                - 단순히 용어 뜻을 묻는 문제보다 개념의 역할, 목적, 차이, 흐름, 적용 상황을 묻는 문제를 우선 생성하세요.
                - questionText는 하나의 명확한 질문 문장으로 작성하세요.
                - 정답은 강의 내용 기준으로 명확해야 합니다.
                - 오답 선택지는 그럴듯해야 하지만 강의 내용 기준으로 명확히 틀려야 합니다.
                - 선택지 간 길이와 문체를 최대한 비슷하게 맞추세요.
                - "모두 정답", "정답 없음", "위 내용 모두", "알 수 없음" 같은 선택지는 사용하지 마세요.
                - 내부 DB 컬럼명, JSON 필드명, 시스템 구현용 변수명은 문제 내용에 노출하지 마세요.
                - 문제, 선택지, 해설은 모두 자연스러운 한국어 문장으로 작성하세요.

                [난이도 기준]
                - EASY: 핵심 용어, 기본 개념, 단순 역할 이해 중심
                - MEDIUM: 개념 간 차이, 사용 목적, 동작 흐름 이해 중심
                - HARD: 상황 판단, 적용 방식, 잘못된 설명 구분 중심

                [출력 규칙]
                - 반드시 JSON 객체 하나만 반환하세요.
                - JSON 외의 설명 문장, 마크다운 코드블록, ```json 표기, 주석은 절대 포함하지 마세요.
                - 아래 JSON 형식과 필드명을 그대로 사용하세요.
                - 필드 누락 없이 모든 값을 채워주세요.

                [반환 JSON 형식]
                {
                  "quizzes": [
                    {
                      "questionType": "MULTIPLE_CHOICE",
                      "questionText": "문제 내용",
                      "difficulty": "%s",
                      "explanation": "문제 전체에 대한 핵심 해설",
                      "answerText": "정답 선택지의 핵심 내용",
                      "options": [
                        {
                          "optionNo": 1,
                          "optionText": "선택지 내용",
                          "correctYn": "N",
                          "explanation": "이 선택지가 오답인 이유"
                        },
                        {
                          "optionNo": 2,
                          "optionText": "선택지 내용",
                          "correctYn": "Y",
                          "explanation": "이 선택지가 정답인 이유"
                        },
                        {
                          "optionNo": 3,
                          "optionText": "선택지 내용",
                          "correctYn": "N",
                          "explanation": "이 선택지가 오답인 이유"
                        },
                        {
                          "optionNo": 4,
                          "optionText": "선택지 내용",
                          "correctYn": "N",
                          "explanation": "이 선택지가 오답인 이유"
                        }
                      ]
                    }
                  ]
                }

                [강의 내용]
                %s
                """.formatted(
                quizCount,
                difficulty,
                difficulty,
                sourceText
        );
    }
}