package com.hlinks.domain.quiz.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChromaClient {

    private final ChromaProperties properties;
    private final RestClient.Builder restClientBuilder;

    private RestClient restClient;
    private volatile String collectionId;

    public void upsert(
            String id,
            String document,
            List<Double> embedding,
            Map<String, Object> metadata
    ) {
        if (!StringUtils.hasText(id)) {
            throw new AiQuizException("Chroma upsert id가 비어 있습니다.");
        }

        if (!StringUtils.hasText(document)) {
            throw new AiQuizException("Chroma upsert document가 비어 있습니다.");
        }

        if (embedding == null || embedding.isEmpty()) {
            throw new AiQuizException("Chroma upsert embedding이 비어 있습니다.");
        }

        ChromaUpsertRequest request = ChromaUpsertRequest.of(
                List.of(id),
                List.of(embedding),
                List.of(document),
                List.of(metadata == null ? Map.of() : metadata)
        );

        getRestClient().post()
                .uri(recordsPath("/upsert"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new AiQuizException("Chroma upsert 요청에 실패했습니다. status=" + res.getStatusCode());
                })
                .toBodilessEntity();
    }

    public List<String> query(
            List<Double> queryEmbedding,
            Long courseId,
            int topK
    ) {
        if (queryEmbedding == null || queryEmbedding.isEmpty()) {
            throw new AiQuizException("Chroma query embedding이 비어 있습니다.");
        }

        int nResults = topK <= 0 ? 5 : topK;

        Map<String, Object> where = courseId == null
                ? Map.of()
                : Map.of("courseId", courseId);

        ChromaQueryRequest request = ChromaQueryRequest.of(
                List.of(queryEmbedding),
                nResults,
                where,
                List.of("documents", "metadatas", "distances")
        );

        ChromaQueryResponse response = getRestClient().post()
                .uri(recordsPath("/query"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new AiQuizException("Chroma query 요청에 실패했습니다. status=" + res.getStatusCode());
                })
                .body(ChromaQueryResponse.class);

        if (response == null || response.getDocuments() == null || response.getDocuments().isEmpty()) {
            return List.of();
        }

        List<List<String>> documents = response.getDocuments();

        if (documents.get(0) == null) {
            return List.of();
        }

        return documents.get(0);
    }

    private String getCollectionId() {
        if (StringUtils.hasText(collectionId)) {
            return collectionId;
        }

        synchronized (this) {
            if (!StringUtils.hasText(collectionId)) {
                collectionId = getOrCreateCollectionId();
            }

            return collectionId;
        }
    }

    private RestClient getRestClient() {
        if (restClient != null) {
            return restClient;
        }

        synchronized (this) {
            if (restClient == null) {
                validateProperties();

                restClient = restClientBuilder
                        .requestFactory(createRequestFactory())
                        .build();
            }

            return restClient;
        }
    }

    private String getOrCreateCollectionId() {
        JsonNode collectionResponse = getRestClient().get()
                .uri(collectionsPath())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new AiQuizException("Chroma collection 목록 조회에 실패했습니다. status=" + res.getStatusCode());
                })
                .body(JsonNode.class);

        List<ChromaCollection> collections = parseCollections(collectionResponse);

        for (ChromaCollection collection : collections) {
            if (properties.getCollectionName().equals(collection.getName())) {
                return collection.getId();
            }
        }

        ChromaCreateCollectionRequest request = new ChromaCreateCollectionRequest(
                properties.getCollectionName()
        );

        ChromaCollection created = getRestClient().post()
                .uri(collectionsPath())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new AiQuizException("Chroma collection 생성에 실패했습니다. status=" + res.getStatusCode());
                })
                .body(ChromaCollection.class);

        if (created == null || !StringUtils.hasText(created.getId())) {
            throw new AiQuizException("Chroma collection id를 가져오지 못했습니다.");
        }

        return created.getId();
    }

    private List<ChromaCollection> parseCollections(JsonNode root) {
        if (root == null || root.isNull() || root.isMissingNode()) {
            return List.of();
        }

        JsonNode collectionNode = root.isArray()
                ? root
                : root.path("collections");

        if (!collectionNode.isArray()) {
            return List.of();
        }

        List<ChromaCollection> collections = new ArrayList<>();

        for (JsonNode item : collectionNode) {
            ChromaCollection collection = new ChromaCollection();
            collection.setId(item.path("id").asText(null));
            collection.setName(item.path("name").asText(null));

            if (StringUtils.hasText(collection.getId()) && StringUtils.hasText(collection.getName())) {
                collections.add(collection);
            }
        }

        return collections;
    }

    private String collectionsPath() {
        return "%s/api/v2/tenants/%s/databases/%s/collections".formatted(
                trimTrailingSlash(properties.getBaseUrl()),
                properties.getTenant(),
                properties.getDatabase()
        );
    }

    private String recordsPath(String suffix) {
        return "%s/%s%s".formatted(
                collectionsPath(),
                getCollectionId(),
                suffix
        );
    }

    private String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }

        return value;
    }

    private SimpleClientHttpRequestFactory createRequestFactory() {
        int timeoutSeconds = properties.getTimeoutSeconds() == null
                ? 10
                : properties.getTimeoutSeconds();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        return factory;
    }

    private void validateProperties() {
        if (!StringUtils.hasText(properties.getBaseUrl())) {
            throw new AiQuizException("hlinks.chroma.base-url 설정이 필요합니다.");
        }

        if (!StringUtils.hasText(properties.getTenant())) {
            throw new AiQuizException("hlinks.chroma.tenant 설정이 필요합니다.");
        }

        if (!StringUtils.hasText(properties.getDatabase())) {
            throw new AiQuizException("hlinks.chroma.database 설정이 필요합니다.");
        }

        if (!StringUtils.hasText(properties.getCollectionName())) {
            throw new AiQuizException("hlinks.chroma.collection-name 설정이 필요합니다.");
        }
    }

    @Getter
    @RequiredArgsConstructor
    private static class ChromaCreateCollectionRequest {
        private final String name;
    }

    @Getter
    @RequiredArgsConstructor(staticName = "of")
    private static class ChromaUpsertRequest {
        private final List<String> ids;
        private final List<List<Double>> embeddings;
        private final List<String> documents;
        private final List<Map<String, Object>> metadatas;
    }

    @Getter
    @RequiredArgsConstructor(staticName = "of")
    private static class ChromaQueryRequest {

        @JsonProperty("query_embeddings")
        private final List<List<Double>> queryEmbeddings;

        @JsonProperty("n_results")
        private final Integer nResults;

        private final Map<String, Object> where;

        private final List<String> include;
    }

    @Getter
    @Setter
    private static class ChromaCollection {
        private String id;
        private String name;
    }

    @Getter
    @Setter
    private static class ChromaQueryResponse {
        private List<List<String>> documents;
    }
}
