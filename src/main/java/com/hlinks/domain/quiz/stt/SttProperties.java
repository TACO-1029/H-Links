package com.hlinks.domain.quiz.stt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "hlinks.stt")
public class SttProperties {

    private String provider = "openai";
    private String apiUrl = "https://api.openai.com/v1/audio/transcriptions";
    private String apiKey;
    private String model = "whisper-1";
    private String language = "ko";
    private long timeoutSeconds = 120;
}
