package com.hlinks.domain.recommend.kcy.type;

import com.hlinks.domain.recommend.kcy.dto.TetrisBlockDto;
import lombok.Getter;
import java.util.List;

@Getter
public enum TetrisBlockRegistry {

    // AI - PROMPTER / OUTLINE / WIDE
    AI_PROMPT_ENG("blk-ai-1", "AI 무제한 구독", "AI", "PROMPTER", 2, 2, List.of(List.of(1,1), List.of(1,1)), "최신 유료 LLM 및 다양한 AI 에이전트 서비스 무제한 이용 지원"),

    // SWE - MANUAL / ACTION / OUTLINE / CORPORATE
    SWE_AGILE("blk-se-2", "애자일 방식", "SWE", "ACTION", 2, 1, List.of(List.of(1,1)), "스프린트 주기마다 유기적으로 계획을 보완하며 완성도를 높이는 기민한 협업 방식"),
    SWE_TDD("blk-se-3", "TDD", "SWE", "ACTION", 3, 1, List.of(List.of(1,1,1)), "구현 전에 테스트 케이스를 먼저 작성하여 소프트웨어 아키텍처의 안정성 견인"),
    SWE_WATERFALL("blk-se-4", "코드리뷰", "SWE", "DEEP", 1, 3, List.of(List.of(1), List.of(1), List.of(1)), "동료 간 코드 품질을 상호 검증하고 지식을 전파하여 오류를 차단하는 꼼꼼한 코드리뷰"),

    // BACKEND - MANUAL / PROMPTER / ACTION / DEEP
    BE_SPRING("blk-be-2", "의존성 주입", "BACKEND", "PROMPTER", 2, 2, List.of(List.of(1,1), List.of(1,1)), "느슨한 결합도를 통해 컴포넌트 간 유연성을 제공하는 스프링 핵심 아키텍처"),
    BE_REDIS("blk-be-4", "성능 최적화", "BACKEND", "DEEP", 2, 1, List.of(List.of(1,1)), "초고속 인메모리 캐싱을 통해 대량의 트래픽에도 안정적인 속도 제공"),
    BE_JPA("blk-be-5", "ORM 설계", "BACKEND", "PROMPTER", 2, 1, List.of(List.of(1,1)), "데이터베이스와 객체 모델을 일관되게 매핑하는 최적의 영속성 컨텍스트 관리"),
    BE_NODE("blk-be-6", "이벤트 처리", "BACKEND", "ACTION", 2, 2, List.of(List.of(1,0), List.of(1,1)), "대규모 비동기 입출력(I/O) 요청을 효율적으로 처리하는 단일 스레드 논블로킹 패러다임"),

    // INFRA - OUTLINE / WIDE
    INFRA_DOCKER("blk-infra-1", "도커", "INFRA", "OUTLINE", 3, 1, List.of(List.of(1,1,1)), "로컬 환경과 서버 개발 환경의 불일치를 해소하는 격리형 컨테이너 환경 표준화 기술"),
    INFRA_TERRAFORM("blk-infra-3", "풀 재택근무", "INFRA", "INDEPENDENT", 2, 1, List.of(List.of(1,1)), "시공간 제약 없이 최상의 업무 몰입감을 유지해 주는 완전 자율형 리모트 근무제"),

    // FRONTEND - ACTION / MANUAL / OUTLINE
    FE_REACT("blk-fe-1", "선언적 UI", "FRONTEND", "ACTION", 2, 2, List.of(List.of(1,1), List.of(1,1)), "상태 변화에 따른 렌더링 과정을 자동화하여 유지보수를 획기적으로 개선하는 컴포넌트 모델"),

    // METHODOLOGY - WIDE / DEEP / OUTLINE / CORPORATE
    METHOD_MERMAID("blk-met-3", "화목한 팀", "METHOD", "CORPORATE", 2, 1, List.of(List.of(1,1)), "서로의 다양성을 배려하고 긍정적인 자극을 공유하며 함께 성장하는 수평적 협업 문화"),
    METHOD_GIT("blk-met-5", "사이드 프로젝트", "METHOD", "CORPORATE", 1, 1, List.of(List.of(1)), "개인의 성장과 지적 호기심 충족을 위해 업무 중 일정 수준 허용되는 기술 탐색 기회"),

    // INDEPENDENT - 자율성/독립형 성향을 대변하는 4종 블록
    INDEP_POC("blk-ind-1", "텍스트 소통", "METHOD", "INDEPENDENT", 2, 1, List.of(List.of(1,1)), "불필요한 미팅을 최소화하고 비동기식 텍스트로 업무 명세를 투명하게 기록하는 방식"),
    INDEP_BOILERPLATE("blk-ind-4", "중복 최소화", "METHOD", "PROMPTER", 1, 2, List.of(List.of(1), List.of(1)), "단순 반복형 보일러플레이트 코드를 줄여 핵심 비즈니스 로직 작성의 몰입도 극대화");

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
