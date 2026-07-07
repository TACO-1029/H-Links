package com.hlinks.domain.career.ai;

import org.springframework.stereotype.Component;

@Component
public class LevelTestPromptBuilder {

    public String build(String skillName, int questionCount, int lowCount, int mediumCount, int highCount) {
        return """
                아래 IT 세부 기술에 관한 학습자 평가용 객관식 레벨테스트 문항을 생성하세요.

                [대상 기술]
                %s
                
                [사전 리서치 및 출제 기준 (매우 중요)]
                가능한 경우 공식 문서의 명세(Specification), 널리 알려진 공식 Best Practice, 공개적으로 잘 알려진
                실제 사례를 기반으로 출제한다. 근거가 불명확한 GitHub Issue나 StackOverflow 내용을 사실처럼 생성하지 않는다.
                
                실제 운영환경 시나리오는 생성할 수 있으나,
                실존 GitHub Issue,
                StackOverflow 질문,
                CVE,
                특정 장애 사례를 사실인 것처럼 만들어서는 안 된다.
                
                실제 사례를 언급하는 경우에는
                널리 알려진 공개 사례만 사용한다.

                [생성 규칙]
                - 반드시 지정된 기술 범위 내에서 질문과 선택지를 구성하세요.
                - 총 생성해야 하는 문항 수는 정확히 %d개입니다.
                - 출제할 문항의 난이도 비율은 다음과 같습니다:
                  - LOW (하): %d문항
                  - MEDIUM (중): %d문항
                  - HIGH (상): %d문항
                - 각 문항의 difficulty 필드값은 위 난이도 구분에 맞추어 대문자 "LOW", "MEDIUM", "HIGH" 중 하나로 매핑하세요.
                - 모든 퀴즈의 questionType은 "MULTIPLE_CHOICE"로 구성하세요.
                - 각 문항의 options는 정확히 4개여야 합니다.
                - 각 문항의 정답 선택지는 correctYn이 "Y"인 항목 정확히 1개만 존재해야 하며, 나머지는 "N"이어야 합니다.
                - optionNo는 반드시 1, 2, 3, 4 숫자를 순서대로 사용하세요.
                - 선택지 구체화: 각 option은 충분한 기술적 정보를 포함하되
                           불필요하게 길게 작성하지 않는다.
                - 정답 문항은 정답 위치가 특정 번호에 편중되지 않도록 균등하게 분산한다.
                - answerText에는 정답 선택지의 핵심 내용 및 정답인 이유를 명확히 작성하세요.
                - explanation에는 정답 이유, 각 오답이 왜 틀렸는지, 관련 동작 원리 정도를 작성하세요.

                [난이도 정의 및 예시]
                LOW (실무 기본 심화)
                신입 수준의 용어 암기 문제는 출제하지 않는다.
                공식 문서의 핵심 개념과 내부 동작 원리를 실제 개발 상황에 적용할 수 있는지를 평가한다.
                단순 API 이름이나 옵션을 묻는 문제가 아니라, 특정 요구사항이나 운영 환경에서 적절한 기능, 설정, 자료구조, 동작 방식을 선택할 수 있는 수준으로 출제한다.
                3~5년 이상의 실무 개발자라면 충분히 해결 가능하지만, 기본 개념이 부족하면 틀릴 수 있는 수준으로 구성한다.
                MEDIUM (실무 운영 및 트러블슈팅)
                실제 운영 환경(Production)에서 발생할 수 있는 성능 저하, 동시성 문제, 메모리 사용량 증가, 데이터 정합성, 네트워크 지연, 프레임워크 내부 동작 등의 원인을 분석하고 해결하는 능력을 평가한다.
                단순한 베스트 프랙티스 암기가 아니라 로그, 코드, 설정, 실행 흐름을 종합적으로 분석해야 정답을 도출할 수 있도록 구성한다.
                5~10년 이상의 실무 경험이 있는 개발자가 실제 장애를 경험했다면 유리한 수준으로 출제한다.
                HIGH (아키텍처 및 내부 구현 심층 이해)
                언어 스펙, 런타임, 컴파일러, 메모리 모델, Proxy, Lock, OS와의 상호작용 등 내부 구현 원리와 설계상의 제약을 깊이 이해하고 있는지를 평가한다.
                특정 조건에서만 발생하는 Edge Case, 대규모 트래픽 환경에서 드러나는 병목, 프레임워크 내부 아키텍처의 한계, 복잡한 동시성 시나리오 등을 기반으로 출제한다.
                단순한 지엽적 암기가 아니라 "왜 이런 현상이 발생하는가"를 이해해야만 해결 가능한 문제로 구성하며, 숙련된 시니어 개발자도 충분히 고민해야 하는 수준으로 출제한다.
                                
                [출력 규칙]
                - 반드시 유효한 단일 JSON 객체 형식만 반환하세요.
                - JSON 외의 부연 설명, 마크다운 코드블록, ```json 표기 등은 모두 제외하고 순수한 JSON 문자열만 반환하세요.
                - 아래 JSON 필드 구조를 준수하세요.

                [반환 JSON 형식]
                {
                  "questions": [
                    {
                      "questionText": "문제 내용",
                      "difficulty": "LOW",
                      "explanation": "해설 내용",
                      "answerText": "정답 텍스트",
                      "options": [
                        {
                          "optionNo": 1,
                          "optionText": "선택지 1",
                          "correctYn": "Y",
                          "explanation": "설명"
                        },
                        {
                          "optionNo": 2,
                          "optionText": "선택지 2",
                          "correctYn": "N",
                          "explanation": "설명"
                        },
                        {
                          "optionNo": 3,
                          "optionText": "선택지 3",
                          "correctYn": "N",
                          "explanation": "설명"
                        },
                        {
                          "optionNo": 4,
                          "optionText": "선택지 4",
                          "correctYn": "N",
                          "explanation": "설명"
                        }
                      ]
                    }
                  ]
                }
                """.formatted(
                skillName,
                questionCount,
                lowCount,
                mediumCount,
                highCount
        );
    }
}
