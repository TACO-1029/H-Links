package com.hlinks.domain.quiz.ai.service;

import com.hlinks.domain.quiz.ai.AiQuizException;
import com.hlinks.domain.quiz.ai.ChromaClient;
import com.hlinks.domain.quiz.ai.OpenAiEmbeddingClient;
import com.hlinks.domain.quiz.ai.TranscriptChunker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseTranscriptIndexService {

    private static final String SOURCE_TYPE = "STT";

    private final TranscriptChunker transcriptChunker;
    private final OpenAiEmbeddingClient embeddingClient;
    private final ChromaClient chromaClient;

    /**
     * STT로 생성된 transcriptText를 chunk 단위로 분리한 뒤
     * 각 chunk를 embedding하여 Chroma Vector DB에 저장한다.
     *
     * 실패하더라도 퀴즈 생성/강의 등록 자체가 중단되지 않도록
     * 호출부에서는 이 메서드의 예외를 fallback 처리하는 것을 권장한다.
     */
    public void index(Long courseId, Long chapterId, String courseTitle, String transcriptText) {
        if (courseId == null) {
            throw new AiQuizException("강의 자막 인덱싱을 위한 courseId가 없습니다.");
        }

        if (chapterId == null) {
            throw new AiQuizException("강의 자막 인덱싱을 위한 chapterId가 없습니다.");
        }

        if (!StringUtils.hasText(transcriptText)) {
            log.warn("강의 자막 인덱싱 생략: transcriptText가 비어 있습니다. courseId={}", courseId);
            return;
        }

        List<String> chunks = transcriptChunker.split(transcriptText);

        if (chunks.isEmpty()) {
            log.warn("강의 자막 인덱싱 생략: 생성된 chunk가 없습니다. courseId={}", courseId);
            return;
        }

        log.info("강의 자막 Chroma 인덱싱 시작. courseId={}, chunkCount={}", courseId, chunks.size());

        for (int i = 0; i < chunks.size(); i++) {
            int chunkIndex = i + 1;
            String chunkText = chunks.get(i);

            indexChunk(courseId, chapterId, courseTitle, chunkIndex, chunkText);
        }

        log.info("강의 자막 Chroma 인덱싱 완료. courseId={}, chunkCount={}", courseId, chunks.size());
    }

    private void indexChunk(
            Long courseId,
            Long chapterId,
            String courseTitle,
            int chunkIndex,
            String chunkText
    ) {
        try {
            List<Double> embedding = embeddingClient.embed(chunkText);

            chromaClient.upsert(
                    buildChunkId(courseId, chapterId, chunkIndex),
                    chunkText,
                    embedding,
                    buildMetadata(courseId, courseTitle, chunkIndex)
            );
        } catch (Exception e) {
            log.warn(
                    "강의 자막 chunk 인덱싱 실패. courseId={}, chunkIndex={}, message={}",
                    courseId,
                    chunkIndex,
                    e.getMessage()
            );
        }
    }

    private String buildChunkId(Long courseId, Long chapterId, int chunkIndex) {
        return "course-%d-chapter-%d-chunk-%d".formatted(courseId, chapterId, chunkIndex);
    }

    private Map<String, Object> buildMetadata(
            Long courseId,
            String courseTitle,
            int chunkIndex
    ) {
        return Map.of(
                "courseId", courseId,
                "courseTitle", safe(courseTitle),
                "chunkIndex", chunkIndex,
                "source", SOURCE_TYPE
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
