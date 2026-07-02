package com.hlinks.domain.quiz.ffmpeg;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "hlinks.ffmpeg")
public class FfmpegProperties {

    private String command = "ffmpeg";
    private String tempDir = "./storage/temp/audio";
    private long timeoutSeconds = 120;
    private long cleanupRetentionHours = 24;
}