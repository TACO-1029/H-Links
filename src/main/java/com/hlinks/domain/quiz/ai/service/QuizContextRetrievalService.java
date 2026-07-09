package com.hlinks.domain.quiz.ai.service;

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
public class QuizContextRetrievalService {

    private static final int DEFAULT_TOP_K = 5;

    private final OpenAiEmbeddingClient embeddingClient;
    private final ChromaClient chromaClient;

    /**
     * 퀴즈 생성에 사용할 Context를 검색한다.
     *
     * Chroma 검색에 성공하면 검색된 chunk context를 반환하고,
     * 실패하거나 검색 결과가 없으면 기존 sourceText를 그대로 반환한다.
     */
    public String retrieveContext(
            Long courseId,
            String difficulty,
            String fallbackText
    ) {
        if (courseId == null) {
            log.warn("퀴즈 Context 검색 생략: courseId가 없습니다.");
            return fallbackText;
        }

        if (!StringUtils.hasText(fallbackText)) {
            log.warn("퀴즈 Context 검색 fallbackText가 비어 있습니다. courseId={}", courseId);
            return "";
        }

        try {
            String queryText = buildQueryText(difficulty);
            List<Double> queryEmbedding = embeddingClient.embed(queryText);

            List<String> retrievedChunks = chromaClient.query(
                    queryEmbedding,
                    courseId,
                    DEFAULT_TOP_K
            );

            if (retrievedChunks == null || retrievedChunks.isEmpty()) {
                log.warn("Chroma 검색 결과 없음. 기존 sourceText로 fallback. courseId={}", courseId);
                return fallbackText;
            }

            String context = buildRetrievedContext(retrievedChunks);

            if (!StringUtils.hasText(context)) {
                return fallbackText;
            }

            log.info("Chroma 기반 퀴즈 Context 검색 완료. courseId={}, chunkCount={}",
                    courseId,
                    retrievedChunks.size()
            );

            return context;
        } catch (Exception e) {
            log.warn("Chroma 기반 퀴즈 Context 검색 실패. 기존 sourceText로 fallback. courseId={}, message={}",
                    courseId,
                    e.getMessage()
            );
            return fallbackText;
        }
    }

    private String buildQueryText(String difficulty) {
        return """
                %s 난이도의 객관식 퀴즈를 만들기 위한 강의 핵심 개념을 찾아줘.
                개념 정의, 처리 흐름, 주요 특징, 주의점, 비교 포인트를 우선 검색해줘.
                """.formatted(StringUtils.hasText(difficulty) ? difficulty : "기본");
    }

    private String buildRetrievedContext(List<String> retrievedChunks) {
        StringBuilder context = new StringBuilder();

        for (int i = 0; i < retrievedChunks.size(); i++) {
            String chunk = retrievedChunks.get(i);

            if (!StringUtils.hasText(chunk)) {
                continue;
            }

            context.append("[Retrieved Chunk ")
                    .append(i + 1)
                    .append("]\n")
                    .append(chunk.trim())
                    .append("\n\n");
        }

        return context.toString().trim();
    }
}