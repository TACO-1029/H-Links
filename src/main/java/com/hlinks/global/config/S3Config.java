package com.hlinks.global.config;

import com.hlinks.global.storage.S3StorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(S3StorageProperties.class)
@ConditionalOnProperty(name = "hlinks.storage.type", havingValue = "s3")
public class S3Config {

    @Bean
    public S3Client s3Client(S3StorageProperties properties) {
        return S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .apiCallTimeout(Duration.ofSeconds(30))
                        .apiCallAttemptTimeout(Duration.ofSeconds(10))
                        .build())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(S3StorageProperties properties) {
        return S3Presigner.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .build();
    }
}
