package com.hlinks.domain.recommend.kcy.type;

import com.hlinks.domain.recommend.kcy.dto.TetrisBlockDto;
import lombok.Getter;
import java.util.List;

@Getter
public enum TetrisBlockRegistry {

    // AI - PROMPTER
    AI_PROMPT_ENG("blk-ai-1", "프롬프트 엔지니어링", "AI", "PROMPTER", 2, 4, List.of(List.of(1,1), List.of(1,1), List.of(1,1), List.of(1,1))),
    AI_HARNESS("blk-ai-2", "하네스 엔지니어링", "AI", "PROMPTER", 4, 3, List.of(List.of(1,1,1,1), List.of(1,1,1,1), List.of(1,1,1,1))),
    AI_MULTI_AGENT("blk-ai-3", "멀티 에이전트", "AI", "OUTLINE", 3, 3, List.of(List.of(1,1,1), List.of(1,0,1), List.of(1,1,1))),
    AI_COPILOT("blk-ai-4", "Copilot", "AI", "PROMPTER", 2, 3, List.of(List.of(1,1), List.of(1,1), List.of(1,1))),

    // SWE - MANUAL / ACTION / OUTLINE
    SWE_QA("blk-se-1", "순수 코드 QA", "SWE", "MANUAL", 1, 3, List.of(List.of(1), List.of(1), List.of(1))),
    SWE_AGILE("blk-se-2", "애자일", "SWE", "ACTION", 2, 1, List.of(List.of(1,1))),
    SWE_TDD("blk-se-3", "TDD", "SWE", "OUTLINE", 3, 2, List.of(List.of(1,1,1), List.of(0,1,0))),

    // BACKEND - MANUAL / PROMPTER
    BE_JDBC("blk-be-1", "Raw JDBC", "BACKEND", "MANUAL", 2, 2, List.of(List.of(1,0), List.of(1,1))),
    BE_SPRING("blk-be-2", "Spring Boot", "BACKEND", "PROMPTER", 2, 2, List.of(List.of(1,1), List.of(1,1))),

    // INFRA - OUTLINE
    INFRA_DOCKER("blk-infra-1", "Docker", "INFRA", "OUTLINE", 3, 1, List.of(List.of(1,1,1))),
    INFRA_K8S("blk-infra-2", "Kubernetes", "INFRA", "OUTLINE", 2, 2, List.of(List.of(0,1), List.of(1,1))),

    // FRONTEND - ACTION / MANUAL
    FE_REACT("blk-fe-1", "React MVP", "FRONTEND", "ACTION", 2, 2, List.of(List.of(1,1), List.of(1,1))),
    FE_VUE("blk-fe-2", "Vue.js", "FRONTEND", "MANUAL", 1, 2, List.of(List.of(1), List.of(1)));

    private final String id;
    private final String name;
    private final String theme;
    private final String targetScoreAxis;
    private final int width;
    private final int height;
    private final List<List<Integer>> layout;

    TetrisBlockRegistry(String id, String name, String theme, String targetScoreAxis, int width, int height, List<List<Integer>> layout) {
        this.id = id;
        this.name = name;
        this.theme = theme;
        this.targetScoreAxis = targetScoreAxis;
        this.width = width;
        this.height = height;
        this.layout = layout;
    }

    public TetrisBlockDto toDto() {
        return new TetrisBlockDto(id, name, theme, targetScoreAxis, width, height, layout);
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
