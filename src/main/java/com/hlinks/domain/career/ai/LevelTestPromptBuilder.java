package com.hlinks.domain.career.ai;

import org.springframework.stereotype.Component;

@Component
public class LevelTestPromptBuilder {

    public String build(String skillName, int questionCount, int lowCount, int mediumCount, int highCount) {
        return """
                아래 IT 세부 기술에 관한 학습자 평가용 객관식 레벨테스트 문항을 생성하세요.

                [대상 기술]
                %s

                [역할]
                당신은 글로벌 빅테크 기업(Google, Netflix, AWS 등)의 시니어 아키텍트이자, 10년 차 이상의 베테랑 개발자를 대상으로 실무에서의 치명적인 트러블슈팅, 대규모 트래픽/데이터 처리 시의 엣지 케이스(Edge Case), 프레임워크 내부 아키텍처의 한계점을 집요하게 파고드는 악명 높은 면접관입니다.

                [사전 리서치 및 출제 기준 (매우 중요)]
                문항을 생성하기 전에 반드시 머릿속으로 다음의 출처들을 검색 및 참고하여 '실제 발생했던 현업의 문제'를 기반으로 출제하세요.
                1. 공식 문서(Official Documentation)의 'Known Issues', 'Best Practices', 'Warning/Caution' 블록
                2. 해당 기술의 GitHub Repository에서 수많은 토론이 오갔던 'Closed Issues(치명적 버그 및 한계점)', 'Pull Requests(핵심 성능 개선 로직)'
                3. Stack Overflow의 심도 있는 토론(Bounty가 걸렸던 문제들) 및 글로벌 IT 기업 Tech Blog의 장애 회고(Post-mortem) 아티클
                4. 특정 버전에서 발생했던 유명한 취약점(CVE)이나 메모리 누수(Memory Leak), 동시성(Concurrency) 이슈

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
                - 정답 문항은 1, 2, 3, 4 중 랜덤한 하나여야 합니다. 일관성이나 규칙 없이 랜덤하게 배정되어야 합니다.
                - answerText에는 정답 선택지의 핵심 내용 및 정답인 이유를 명확히 작성하세요.
                - explanation에는 문제 전체에 대한 해설, 오답이 왜 오답인지, 관련 공식 레퍼런스나 동작 원리를 상세히 작성하세요.

                [객관성 및 난이도 조절 규칙 (필수 준수)]
                - 주관성 전면 배제: "가장 효과적인 것?", "가장 널리/일반적으로 사용되는 것?", "가장 권장되는 것?"과 같이 논란의 여지가 있거나 상황에 따라 정답이 변할 수 있는 주관적 표현의 출제는 절대 금지합니다.
                - 100%% 팩트 기반 정답: 정답은 공식 레퍼런스 문서, 언어/프레임워크의 명세(Specification), 또는 컴퓨터 시스템의 결정론적 동작에 기인한 반박 불가능한 유일한 사실(Fact)이어야 합니다.
                - 지문 길이와 시나리오를 통한 난이도 상향: 문제를 꼬거나 지엽적인 암기를 요구하는 대신, 구체적인 가상 프로젝트의 고하중 시나리오, 트러블슈팅 코드 스레드, 아키텍처 다이어그램 설명 등을 지문에 5~6줄 이상으로 상세히 서술하여 꼼꼼히 읽어야만 동작 상태를 파악할 수 있도록 만드세요.
                - 선택지 구체화: 보기(optionText) 역시 단답형이 아닌, 각각 2~3줄 가량의 상세한 기술적 구현 방식과 그에 따른 부작용(Side Effect), 또는 한계점을 기술적으로 정밀하게 서술하여 학습자가 신중히 읽고 비교 소거해야 하도록 길게 구성하세요.

                [난이도 정의 및 예시]
                - LOW: 공식 문서의 핵심 아키텍처 및 내부 동작 원리 이해. (단순 용어 묻기가 아닌, "이러한 요구사항에서 어떤 설정/메서드를 사용해야 하는가?"와 같은 실전 적용 수준)
                - MEDIUM: 실무 환경의 복잡한 트러블슈팅, 최적화 방안, 에러 분석 및 응용 아키텍처. 5~10년 차 이상의 시니어 레벨이 실제 운영 환경(Production)에서 겪어봤을 법한 동시성 이슈, GC/메모리 튜닝, N+1 문제 등의 원인과 해결책.
                - HIGH: 극한의 매운맛 마라맛 난이도. "이걸 내가 어떻게 알아?" 느낌이 들 정도로 깊은 지식. 예를 들어, 언어나 프레임워크의 OS/커널 레벨 상호작용, 리플렉션/프록시의 극단적 엣지 케이스, 잘 알려지지 않은 설계적 결함이나 특정 조건에서만 발생하는 데드락(Deadlock) 시나리오. (단, 억지스러운 지엽적 암기가 아니라, 알면 무릎을 탁 치게 되는 '이유 있는 깊이'여야 함)

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
