package com.hlinks.domain.quiz.stt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hlinks.domain.quiz.stt.dto.SttTranscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SttService {

    private final SttProperties properties;

    public String transcribe(Path audioPath) {
        validateAudioFile(audioPath);
        validateApiKey();

        RestClient restClient = createRestClient();

        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(audioPath.toFile()));
        body.add("model", properties.getModel());
        body.add("language", properties.getLanguage());
        body.add("response_format", "json");

        try {
            String rawResponse = restClient.post()
                    .uri(properties.getApiUrl())
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            System.out.println("===== STT raw response =====");
            System.out.println(rawResponse);

            SttTranscriptionResponse response =
                    new ObjectMapper().readValue(rawResponse, SttTranscriptionResponse.class);

            if (response == null || response.getText() == null || response.getText().isBlank()) {
                throw new SttException("STT 결과 텍스트가 비어 있습니다.");
            }

            return response.getText();

        } catch (Exception e) {
            throw new SttException("STT 변환 요청에 실패했습니다.", e);
        }
    }

    private void validateAudioFile(Path audioPath) {
        if (audioPath == null) {
            throw new SttException("STT 대상 오디오 파일 경로가 비어 있습니다.");
        }

        Path normalizedPath = audioPath.toAbsolutePath().normalize();

        if (!Files.exists(normalizedPath)) {
            throw new SttException("STT 대상 오디오 파일이 존재하지 않습니다. path=" + normalizedPath);
        }

        if (!Files.isRegularFile(normalizedPath)) {
            throw new SttException("STT 대상이 일반 파일이 아닙니다. path=" + normalizedPath);
        }

        if (!Files.isReadable(normalizedPath)) {
            throw new SttException("STT 대상 오디오 파일을 읽을 수 없습니다. path=" + normalizedPath);
        }
    }

    private void validateApiKey() {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new SttException("STT API Key가 설정되어 있지 않습니다.");
        }
    }

    private RestClient createRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = Math.toIntExact(Duration.ofSeconds(properties.getTimeoutSeconds()).toMillis());

        requestFactory.setConnectTimeout(timeoutMillis);
        requestFactory.setReadTimeout(timeoutMillis);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}