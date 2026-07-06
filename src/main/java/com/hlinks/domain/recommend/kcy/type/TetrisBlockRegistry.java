package com.hlinks.domain.recommend.kcy.type;

import com.hlinks.domain.recommend.kcy.dto.TetrisBlockDto;
import lombok.Getter;
import java.util.List;

@Getter
public enum TetrisBlockRegistry {

    // AI - PROMPTER / WIDE / OUTLINE
    AI_PROMPT_ENG("blk-ai-1", "프롬프트 엔지니어링", "AI", "PROMPTER", 2, 2, List.of(List.of(1,1), List.of(1,1)), "LLM의 답변을 원하는 형태로 이끌어내기 위한 프롬프트 튜닝 기법"),
    AI_HARNESS("blk-ai-2", "하네스 엔지니어링", "AI", "PROMPTER", 2, 1, List.of(List.of(1,1)), "AI 성능 평가 및 테스트 자동화를 위한 프레임워크 구축"),
    AI_MULTI_AGENT("blk-ai-3", "멀티 에이전트", "AI", "OUTLINE", 3, 1, List.of(List.of(1,1,1)), "여러 자율 AI 에이전트 간 협력 시나리오를 구성하는 설계 패턴"),
    AI_COPILOT("blk-ai-4", "Copilot", "AI", "PROMPTER", 1, 2, List.of(List.of(1), List.of(1)), "코딩 지원 도구를 활용해 인라인 제안을 실시간으로 수용하여 개발 가속화"),
    AI_RAG("blk-ai-5", "RAG 패턴", "AI", "WIDE", 2, 2, List.of(List.of(1,0), List.of(1,1)), "외부 지식 베이스를 연동하여 AI 답변의 정확성과 팩트를 보강하는 기법"),
    AI_LLMOPS("blk-ai-6", "LLMOps", "AI", "OUTLINE", 2, 1, List.of(List.of(1,1)), "LLM 서비스의 지속적 모니터링, 버전 관리 및 배포 파이프라인 관리"),

    // SWE - MANUAL / ACTION / OUTLINE
    SWE_QA("blk-se-1", "순수 코드 QA", "SWE", "MANUAL", 1, 2, List.of(List.of(1), List.of(1)), "코드의 결함과 비즈니스 로직 안전성을 꼼꼼하게 직접 검증하는 품질 보증"),
    SWE_AGILE("blk-se-2", "애자일 스프린트", "SWE", "ACTION", 2, 1, List.of(List.of(1,1)), "주 단위 스프린트와 피드백 중심의 점진적인 릴리즈 개발 방식"),
    SWE_TDD("blk-se-3", "TDD", "SWE", "OUTLINE", 3, 1, List.of(List.of(1,1,1)), "테스트 코드를 먼저 작성한 뒤 비즈니스 로직을 빌드하는 점진적 설계"),
    SWE_WATERFALL("blk-se-4", "폭포수 모델", "SWE", "OUTLINE", 1, 3, List.of(List.of(1), List.of(1), List.of(1)), "요구분석부터 테스트까지 순차적으로 문서를 기반으로 꼼꼼히 밟아가는 개발 방식"),
    SWE_SCRUM("blk-se-5", "스크럼 미팅", "SWE", "CORPORATE", 2, 2, List.of(List.of(1,1), List.of(1,1)), "매일 짧은 아침 스탠드업 미팅을 통해 팀원 간 현황과 장애물을 공유하는 협업"),

    // BACKEND - MANUAL / PROMPTER / ACTION / DEEP
    BE_JDBC("blk-be-1", "Raw JDBC", "BACKEND", "MANUAL", 2, 1, List.of(List.of(1,1)), "ORM 프레임워크 없이 직접 로우 쿼리를 작성하여 최고의 튜닝 효율을 내는 기법"),
    BE_SPRING("blk-be-2", "Spring Boot", "BACKEND", "PROMPTER", 2, 2, List.of(List.of(1,1), List.of(1,1)), "한국 엔터프라이즈의 표준 백엔드 생태계를 탄탄한 아키텍처로 조립"),
    BE_JUNIT("blk-be-3", "JUnit 테스트", "BACKEND", "ACTION", 1, 2, List.of(List.of(1), List.of(1)), "자바 백엔드 메서드의 기대 동작을 견고하게 검증하는 단위테스트 프레임워크"),
    BE_REDIS("blk-be-4", "Redis 캐시", "BACKEND", "DEEP", 2, 1, List.of(List.of(1,1)), "자주 조회되는 무거운 데이터를 인메모리에 올려 응답 성능을 획기적으로 개선"),
    BE_JPA("blk-be-5", "JPA ORM", "BACKEND", "PROMPTER", 2, 1, List.of(List.of(1,1)), "SQL 중심 개발에서 벗어나 엔티티 매핑을 통해 가독성을 높이는 자바 ORM 표준"),
    BE_NODE("blk-be-6", "Node Express", "BACKEND", "ACTION", 2, 2, List.of(List.of(1,0), List.of(1,1)), "자바스크립트 싱글 스레드 이벤트 루프 기반의 경량 백엔드 API 서버"),

    // INFRA - OUTLINE / WIDE
    INFRA_DOCKER("blk-infra-1", "Docker", "INFRA", "OUTLINE", 3, 1, List.of(List.of(1,1,1)), "환경 의존성 없이 어디서든 동일하게 실행되도록 격리하는 컨테이너 가상화"),
    INFRA_K8S("blk-infra-2", "Kubernetes", "INFRA", "OUTLINE", 2, 2, List.of(List.of(1,1), List.of(1,1)), "수많은 서버와 서비스 컨테이너들의 배포, 스케일링을 관리하는 오케스트레이션"),
    INFRA_TERRAFORM("blk-infra-3", "Terraform IaC", "INFRA", "OUTLINE", 2, 1, List.of(List.of(1,1)), "인프라 구성을 코드로 작성하여 형상 관리하고 손쉽게 선언적 배포"),
    INFRA_AWS("blk-infra-4", "AWS 클라우드", "INFRA", "WIDE", 2, 2, List.of(List.of(0,1), List.of(1,1)), "전 세계 퍼블릭 클라우드 인프라 자원을 유연하게 구성하는 글로벌 스택"),

    // FRONTEND - ACTION / MANUAL / OUTLINE
    FE_REACT("blk-fe-1", "React MVP", "FRONTEND", "ACTION", 2, 2, List.of(List.of(1,1), List.of(1,1)), "가상 DOM을 통해 화면 구성 요소들을 재사용성 높은 컴포넌트 단위로 개발"),
    FE_VUE("blk-fe-2", "Vue.js", "FRONTEND", "MANUAL", 1, 2, List.of(List.of(1), List.of(1)), "러닝 커브가 낮고 양방향 데이터 바인딩을 지원하는 프론트엔드 프레임워크"),
    FE_HTML5("blk-fe-3", "Vanilla JS", "FRONTEND", "MANUAL", 1, 1, List.of(List.of(1)), "추가 프레임워크 라이브러리 없이 바닐라 자바스크립트로 가볍게 화면 제어"),
    FE_TAILWIND("blk-fe-4", "Tailwind CSS", "FRONTEND", "ACTION", 2, 1, List.of(List.of(1,1)), "클래스명 선언만으로 빠르고 감각적인 UI 스타일링을 가속하는 유틸리티 CSS"),
    FE_NEXT("blk-fe-5", "Next.js", "FRONTEND", "OUTLINE", 2, 2, List.of(List.of(1,1), List.of(1,1)), "서버 사이드 렌더링(SSR)을 통해 빠른 초기 로딩과 검색엔진 최적화를 지원"),

    // SECURITY - DEEP / CORPORATE
    SEC_CODING("blk-sec-1", "시큐어 코딩", "SECURITY", "DEEP", 2, 1, List.of(List.of(1,1)), "보안 취약점과 해킹 공격을 소스코드 작성 수준에서 방지하는 정밀 개발"),
    SEC_OAUTH("blk-sec-2", "OAuth2 인증", "SECURITY", "CORPORATE", 2, 1, List.of(List.of(1,1)), "소셜 로그인 등 외부 인증 기관과의 안전한 토큰 기반 회원 식별 체계"),

    // METHODOLOGY - WIDE / DEEP / OUTLINE / CORPORATE
    METHOD_BRAIN("blk-met-1", "브레인스토밍", "METHOD", "WIDE", 2, 2, List.of(List.of(1,1), List.of(0,1)), "아이디어 수렴 이전에 자유롭고 폭넓게 창의적인 발상을 꺼내는 발산 기법"),
    METHOD_DOMAIN("blk-met-2", "도메인 정밀조사", "METHOD", "DEEP", 1, 3, List.of(List.of(1), List.of(1), List.of(1)), "소프트웨어 작성 전에 비즈니스 규칙과 실무 지식을 깊이 있게 조사하는 활동"),
    METHOD_MERMAID("blk-met-3", "Mermaid 차트", "METHOD", "OUTLINE", 2, 1, List.of(List.of(1,1)), "텍스트 형식의 선언문으로 UML 설계도와 아키텍처 흐름도를 자동 렌더링"),
    METHOD_PAIR("blk-met-4", "페어 코딩", "METHOD", "CORPORATE", 1, 2, List.of(List.of(1), List.of(1)), "두 명의 개발자가 한 컴퓨터에서 실시간 코드 리뷰를 병행하며 공동 개발"),
    METHOD_GIT("blk-met-5", "깃헙 협업", "METHOD", "CORPORATE", 1, 1, List.of(List.of(1)), "브랜치 전략을 통해 팀원 간 코드를 정교하게 검토하고 병합하는 협업 프레임"),

    // INDEPENDENT - 자율성/독립형 성향을 대변하는 4종 블록 (기존 디지털노마드 대체 고도화)
    INDEP_POC("blk-ind-1", "단독 PoC 검증", "METHOD", "INDEPENDENT", 2, 1, List.of(List.of(1,1)), "기술적 리스크를 먼저 제거하기 위해 자율적으로 단독 프로토타입을 신속히 검증"),
    INDEP_CUSTOM_LIB("blk-ind-2", "자체 모듈 설계", "SWE", "INDEPENDENT", 2, 1, List.of(List.of(1,1)), "외부 의존성을 낮추기 위해 나만의 공통 모듈과 커스텀 유틸리티를 직접 개발"),
    INDEP_REFACTOR("blk-ind-3", "자발적 코드 개선", "SWE", "INDEPENDENT", 2, 2, List.of(List.of(1,0), List.of(1,1)), "누가 시키지 않아도 기술 부채를 스스로 인지하고 주도적으로 리팩토링을 집도"),
    INDEP_BOILERPLATE("blk-ind-4", "보일러플레이트", "METHOD", "INDEPENDENT", 1, 2, List.of(List.of(1), List.of(1)), "반복되는 초기 세팅과 인프라 기본 환경 코드를 직접 템플릿화하여 자율 배포");

    private final String id;
    private final String name;
    private final String theme;
    private final String targetScoreAxis;
    private final int width;
    private final int height;
    private final List<List<Integer>> layout;
    private final String description;

    TetrisBlockRegistry(String id, String name, String theme, String targetScoreAxis, int width, int height, List<List<Integer>> layout, String description) {
        this.id = id;
        this.name = name;
        this.theme = theme;
        this.targetScoreAxis = targetScoreAxis;
        this.width = width;
        this.height = height;
        this.layout = layout;
        this.description = description;
    }

    public TetrisBlockDto toDto() {
        return new TetrisBlockDto(id, name, theme, targetScoreAxis, width, height, layout, description);
    }

    public static TetrisBlockRegistry findById(String id) {
        for (TetrisBlockRegistry block : values()) {
            if (block.getId().equals(id)) {
                return block;
            }
        }
        return null;
    }
}
