package com.hlinks.domain.career.ai.service;

import com.hlinks.domain.quiz.ai.ChromaClient;
import com.hlinks.domain.quiz.ai.OpenAiEmbeddingClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareerLevelTestContextService {

    private static final int DEFAULT_TOP_K = 5;

    private final OpenAiEmbeddingClient embeddingClient;
    private final ChromaClient chromaClient;

    public String retrieveContext(String skillName, String difficulty) {
        if (!StringUtils.hasText(skillName)) {
            return "";
        }

        try {
            List<Double> queryEmbedding = embeddingClient.embed(buildQueryText(skillName, difficulty));
            List<String> chunks = chromaClient.query(queryEmbedding, null, DEFAULT_TOP_K);

            if (chunks == null || chunks.isEmpty()) {
                log.warn("레벨테스트 Chroma context 검색 결과 없음. skillName={}", skillName);
                return "";
            }

            log.info("레벨테스트 Chroma context 검색 완료. skillName={}, chunkCount={}", skillName, chunks.size());

            return buildContext(chunks);
        } catch (Exception e) {
            log.warn(
                    "레벨테스트 Chroma context 검색 실패. skillName={}, message={}",
                    skillName,
                    e.getMessage()
            );
            return "";
        }
    }

    private String buildQueryText(String skillName, String difficulty) {
        return """
                %s 기술에 대한 %s 난이도 레벨테스트 문제를 만들기 위한 핵심 강의 내용을 찾아줘.
                개념 정의, 동작 원리, 실무 적용, 장애/트러블슈팅, 성능/보안 주의점을 우선 검색해줘.
                """.formatted(
                skillName.trim(),
                StringUtils.hasText(difficulty) ? difficulty.trim() : "기본"
        );
    }

    private String buildContext(List<String> chunks) {
        StringBuilder context = new StringBuilder();

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);

            if (!StringUtils.hasText(chunk)) {
                continue;
            }

            context.append("[Course Context ")
                    .append(i + 1)
                    .append("]\n")
                    .append(chunk.trim())
                    .append("\n\n");
        }

        return context.toString().trim();
    }
}
