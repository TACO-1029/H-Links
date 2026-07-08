package com.hlinks.domain.quiz.ai;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TranscriptChunker {

    private static final int DEFAULT_CHUNK_SIZE = 900;
    private static final int MIN_LAST_CHUNK_SIZE = 200;

    public List<String> split(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String normalized = normalize(text);
        List<String> chunks = new ArrayList<>();

        int start = 0;

        while (start < normalized.length()) {
            int end = Math.min(start + DEFAULT_CHUNK_SIZE, normalized.length());

            if (end < normalized.length()) {
                end = findBestSplitPoint(normalized, start, end);
            }

            String chunk = normalized.substring(start, end).trim();

            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }

            start = end;
        }

        return mergeSmallLastChunk(chunks);
    }

    public String buildContext(String transcriptText, int limit) {
        List<String> chunks = split(transcriptText);

        if (chunks.isEmpty()) {
            return "";
        }

        int contextLimit = Math.max(1, limit);
        StringBuilder context = new StringBuilder();

        for (int i = 0; i < Math.min(chunks.size(), contextLimit); i++) {
            context.append("[Chunk ")
                    .append(i + 1)
                    .append("]\n")
                    .append(chunks.get(i))
                    .append("\n\n");
        }

        return context.toString().trim();
    }

    private String normalize(String text) {
        return text
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private int findBestSplitPoint(String text, int start, int end) {
        int splitPoint = max(
                text.lastIndexOf("\n\n", end),
                text.lastIndexOf("\n", end),
                text.lastIndexOf("다.", end),
                text.lastIndexOf("요.", end),
                text.lastIndexOf(".", end),
                text.lastIndexOf("?", end),
                text.lastIndexOf("!", end)
        );

        if (splitPoint <= start) {
            return end;
        }

        int remainingLengthFromSplit = end - splitPoint;

        if (remainingLengthFromSplit > MIN_LAST_CHUNK_SIZE) {
            return end;
        }

        return splitPoint + 1;
    }

    private List<String> mergeSmallLastChunk(List<String> chunks) {
        if (chunks.size() < 2) {
            return chunks;
        }

        int lastIndex = chunks.size() - 1;
        String last = chunks.get(lastIndex);

        if (last.length() >= MIN_LAST_CHUNK_SIZE) {
            return chunks;
        }

        String previous = chunks.get(lastIndex - 1);
        chunks.set(lastIndex - 1, previous + "\n" + last);
        chunks.remove(lastIndex);

        return chunks;
    }

    private int max(int... values) {
        int max = -1;

        for (int value : values) {
            if (value > max) {
                max = value;
            }
        }

        return max;
    }
}