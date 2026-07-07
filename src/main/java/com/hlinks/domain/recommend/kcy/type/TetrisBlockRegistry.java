package com.hlinks.domain.recommend.kcy.type;

import com.hlinks.domain.recommend.kcy.dto.TetrisBlockDto;
import lombok.Getter;
import java.util.List;

@Getter
public enum TetrisBlockRegistry {

    // AI - PROMPTER / OUTLINE / WIDE
    AI_PROMPT_ENG("blk-ai-1", "AI 무제한 구독", "AI", "PROMPTER", 2, 2, List.of(List.of(1,1), List.of(1,1)), ""),
    AI_HARNESS("blk-ai-2", "사내 프롬프트 템플릿", "AI", "PROMPTER", 2, 1, List.of(List.of(1,1)), "AI가 최적의 아웃풋을 낼 수 있도록 컨텍스트와 지시문을 세밀하게 튜닝하는 환경"),
    AI_MULTI_AGENT("blk-ai-3", "업무 자동화", "AI", "OUTLINE", 3, 1, List.of(List.of(1,1,1)), "멀티 에이전트 워크플로우 자동화: 복잡한 파이프라인을 자율 협업형 AI 에이전트 구조로 설계하는 태스크"),
    AI_COPILOT("blk-ai-4", "AI 페어 프로그래밍", "AI", "PROMPTER", 1, 2, List.of(List.of(1), List.of(1)), ""),
    AI_RAG("blk-ai-5", "사내 RAG 구축", "AI", "OUTLINE", 2, 2, List.of(List.of(1,0), List.of(1,1)), "외부 데이터와 LLM을 연동하여 할루시네이션(왜곡)을 방지하는 맥락 제어"),
    AI_LLMOPS("blk-ai-6", "최신 스택 도입", "AI", "WIDE", 2, 1, List.of(List.of(1,1)), ""),

    // SWE - MANUAL / ACTION / OUTLINE / CORPORATE
    SWE_QA("blk-se-1", "완벽한 기획서", "SWE", "OUTLINE", 1, 2, List.of(List.of(1), List.of(1)), ""),
    SWE_AGILE("blk-se-2", "애자일 방식", "SWE", "ACTION", 2, 1, List.of(List.of(1,1)), ""),
    SWE_TDD("blk-se-3", "TDD", "SWE", "ACTION", 3, 1, List.of(List.of(1,1,1)), "구현 전에 테스트 케이스를 먼저 작성하여 아키텍처의 안정성을 견인"),
    SWE_WATERFALL("blk-se-4", "꼼꼼한 코드리뷰", "SWE", "DEEP", 1, 3, List.of(List.of(1), List.of(1), List.of(1)), ""),
    SWE_SCRUM("blk-se-5", "스크럼 미팅", "SWE", "ACTION", 2, 2, List.of(List.of(1,1), List.of(1,1)), ""),

    // BACKEND - MANUAL / PROMPTER / ACTION / DEEP
    BE_JDBC("blk-be-1", "레거시 제로", "BACKEND", "MANUAL", 2, 1, List.of(List.of(1,1)), "항상 신규개발"),
    BE_SPRING("blk-be-2", "의존성 주입", "BACKEND", "PROMPTER", 2, 2, List.of(List.of(1,1), List.of(1,1)), "(DI) 기반 유연한 아키텍처"),
    BE_JUNIT("blk-be-3", "멘토링", "BACKEND", "CORPORATE", 1, 2, List.of(List.of(1), List.of(1)), "뛰어난 시니어 개발자의 멘토링"),
    BE_REDIS("blk-be-4", "성능 최적화", "BACKEND", "DEEP", 2, 1, List.of(List.of(1,1)), "인메모리 캐싱 기반"),
    BE_JPA("blk-be-5", "ORM 설계", "BACKEND", "PROMPTER", 2, 1, List.of(List.of(1,1)), "데이터 모델 추상화 및 객체 지향 패러다임에 맞춰 영속성 컨텍스트와 테이블 관계를 영리하게 매핑"),
    BE_NODE("blk-be-6", "이벤트 기반 처리", "BACKEND", "ACTION", 2, 2, List.of(List.of(1,0), List.of(1,1)), "비동기 논블로킹 처리"),

    // INFRA - OUTLINE / WIDE
    INFRA_DOCKER("blk-infra-1", "도커", "INFRA", "OUTLINE", 3, 1, List.of(List.of(1,1,1)), "환경 차이로 인한 배포 오류를 원천 차단하는 격리형 컨테이너 환경 표준화 구축"),
    INFRA_K8S("blk-infra-2", "무중단 자동 스케일링 오케스트레이션", "INFRA", "OUTLINE", 2, 2, List.of(List.of(1,1), List.of(1,1)), ""),
    INFRA_TERRAFORM("blk-infra-3", "풀 재택근무", "INFRA", "INDEPENDENT", 2, 1, List.of(List.of(1,1)), ""),
    INFRA_AWS("blk-infra-4", "글로벌 분산 클라우드 자원 설계", "INFRA", "WIDE", 2, 2, List.of(List.of(0,1), List.of(1,1)), "단일 서버 한계를 넘어 전 세계 네트워크망을 유연하게 활용하는 거시적 인프라"),

    // FRONTEND - ACTION / MANUAL / OUTLINE
    FE_REACT("blk-fe-1", "선언적 컴포넌트 아키텍처", "FRONTEND", "ACTION", 2, 2, List.of(List.of(1,1), List.of(1,1)), ""),
    FE_VUE("blk-fe-2", "반응형 상태 관리 패러다임", "FRONTEND", "MANUAL", 1, 2, List.of(List.of(1), List.of(1)), "데이터의 흐름과 UI 바인딩 구조를 직관적이고 꼼꼼하게 통제하는 환경"),
    FE_HTML5("blk-fe-3", "웹 표준 및 웹 접근성(A11y) 준수", "FRONTEND", "MANUAL", 1, 1, List.of(List.of(1)), ""),
    FE_TAILWIND("blk-fe-4", "유틸리티 퍼스트 디자인 시스템", "FRONTEND", "ACTION", 2, 1, List.of(List.of(1,1)), "인라인 마크업 구조를 활용해 화면 레이아웃을 지체 없이 초고속으로 빌드"),
    FE_NEXT("blk-fe-5", "SSR", "FRONTEND", "OUTLINE", 2, 2, List.of(List.of(1,1), List.of(1,1)), "서버 사이드 렌더링(SSR) 및 SEO 최적화"),

    // SECURITY - DEEP / CORPORATE
    SEC_CODING("blk-sec-1", "시큐어 코딩", "SECURITY", "DEEP", 2, 1, List.of(List.of(1,1)), "모든 외부 입력은 위험하다는 전제하에 시스템 심연의 취약점까지 추적 및 방어"),
    SEC_OAUTH("blk-sec-2", "성장하는 조직", "SECURITY", "CORPORATE", 2, 1, List.of(List.of(1,1)), "탄탄한 캐시카우 비즈니스 보유"),

    // METHODOLOGY - WIDE / DEEP / OUTLINE / CORPORATE
    METHOD_BRAIN("blk-met-1", "새로운 아키텍처 패러다임 적극 도입", "METHOD", "WIDE", 2, 2, List.of(List.of(1,1), List.of(0,1)), "기존 틀에 갇히지 않고 새로운 오픈소스나 트렌디한 방법론을 발 빠르게 실험"),
    METHOD_DOMAIN("blk-met-2", "비즈니스 도메인 심층 모델링", "METHOD", "DEEP", 1, 3, List.of(List.of(1), List.of(1), List.of(1)), "기술적 기교보다 해결하려는 비즈니스의 복잡도와 핵심 업무 로직에 딥다이브"),
    METHOD_MERMAID("blk-met-3", "화목한 팀", "METHOD", "CORPORATE", 2, 1, List.of(List.of(1,1)), ""),
    METHOD_PAIR("blk-met-4", "실시간 동료 코드 공유", "METHOD", "CORPORATE", 1, 2, List.of(List.of(1), List.of(1)), ""),
    METHOD_GIT("blk-met-5", "개인 프로젝트 권장", "METHOD", "CORPORATE", 1, 1, List.of(List.of(1)), "회사 업무에 지장을 주지 않는다면 업무 시간 내 개인의 성장을 위한 사이드 프로젝트 개발 환경 적극 권장"),

    // INDEPENDENT - 자율성/독립형 성향을 대변하는 4종 블록
    INDEP_POC("blk-ind-1", "텍스트 중심 소통", "METHOD", "INDEPENDENT", 2, 1, List.of(List.of(1,1)), "잦은 미팅과 구두 보고를 지양하고 슬랙/노션 기반의 비동기식 단독 업무 처리를 선호"),
    INDEP_CUSTOM_LIB("blk-ind-2", "마이크로매니징 없음", "SWE", "INDEPENDENT", 2, 1, List.of(List.of(1,1)), ""),
    INDEP_REFACTOR("blk-ind-3", "불필요한 문서작업 없음", "SWE", "ACTION", 2, 2, List.of(List.of(1,0), List.of(1,1)), ""),
    INDEP_BOILERPLATE("blk-ind-4", "보일러플레이트 제거", "METHOD", "PROMPTER", 1, 2, List.of(List.of(1), List.of(1)), "");
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
