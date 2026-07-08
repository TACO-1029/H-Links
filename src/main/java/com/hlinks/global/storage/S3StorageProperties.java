package com.hlinks.global.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "hlinks.storage.s3")
public class S3StorageProperties {

    private String bucket;
    private String region;
    private String prefix = "";
    private long presignedUrlExpirationMinutes = 10;
}
