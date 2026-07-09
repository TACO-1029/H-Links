package com.hlinks.domain.quiz.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "hlinks.chroma")
public class ChromaProperties {

    private String baseUrl;

    private String tenant = "default_tenant";

    private String database = "default_database";

    private String collectionName = "hlinks_course_transcript_chunks";

    private Integer timeoutSeconds = 10;
}