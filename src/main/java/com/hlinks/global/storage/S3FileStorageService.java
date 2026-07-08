package com.hlinks.global.storage;

import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "hlinks.storage.type", havingValue = "s3")
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3StorageProperties properties;

    @Override
    public StoredFile upload(String key, MultipartFile file) {
        String objectKey = toObjectKey(key);

        try {
            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .contentLength(file.getSize());

            if (StringUtils.hasText(file.getContentType())) {
                requestBuilder.contentType(file.getContentType());
            }

            s3Client.putObject(requestBuilder.build(), RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return new StoredFile(key, file.getOriginalFilename(), file.getContentType(), file.getSize());
        } catch (IOException e) {
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "S3 업로드 파일을 읽지 못했습니다.", e);
        } catch (AwsServiceException e) {
            log.error(
                    "S3 파일 업로드 실패. bucket={}, key={}, statusCode={}, awsErrorCode={}, requestId={}, message={}",
                    properties.getBucket(),
                    objectKey,
                    e.statusCode(),
                    e.awsErrorDetails() == null ? null : e.awsErrorDetails().errorCode(),
                    e.requestId(),
                    e.awsErrorDetails() == null ? e.getMessage() : e.awsErrorDetails().errorMessage(),
                    e
            );
            throw new BaseException(
                    ErrorResponseCode.INTERNAL_SERVER_ERROR,
                    "S3 파일 업로드에 실패했습니다. AWS errorCode="
                            + (e.awsErrorDetails() == null ? "UNKNOWN" : e.awsErrorDetails().errorCode()),
                    e
            );
        } catch (SdkClientException e) {
            log.error("S3 클라이언트 업로드 실패. bucket={}, key={}, message={}", properties.getBucket(), objectKey, e.getMessage(), e);
            throw new BaseException(
                    ErrorResponseCode.INTERNAL_SERVER_ERROR,
                    "S3 파일 업로드에 실패했습니다. AWS 자격 증명 또는 네트워크 설정을 확인해 주세요.",
                    e
            );
        } catch (Exception e) {
            log.error("S3 파일 업로드 실패. bucket={}, key={}", properties.getBucket(), objectKey, e);
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "S3 파일 업로드에 실패했습니다.", e);
        }
    }

    @Override
    public DownloadedFile download(String key) {
        String objectKey = toObjectKey(key);

        try {
            String suffix = extractSuffix(key);
            Path tempFile = Files.createTempFile("hlinks-s3-", suffix);
            Files.deleteIfExists(tempFile);
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .build();

            s3Client.getObject(request, tempFile);
            return new DownloadedFile(tempFile, true);
        } catch (IOException e) {
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "S3 임시 파일 생성에 실패했습니다.", e);
        } catch (AwsServiceException e) {
            log.error(
                    "S3 파일 다운로드 실패. bucket={}, key={}, statusCode={}, awsErrorCode={}, requestId={}, message={}",
                    properties.getBucket(),
                    objectKey,
                    e.statusCode(),
                    e.awsErrorDetails() == null ? null : e.awsErrorDetails().errorCode(),
                    e.requestId(),
                    e.awsErrorDetails() == null ? e.getMessage() : e.awsErrorDetails().errorMessage(),
                    e
            );
            throw new BaseException(
                    ErrorResponseCode.NOT_FOUND_ENDPOINT,
                    "S3 파일을 다운로드하지 못했습니다. AWS errorCode="
                            + (e.awsErrorDetails() == null ? "UNKNOWN" : e.awsErrorDetails().errorCode()),
                    e
            );
        } catch (SdkClientException e) {
            log.error("S3 클라이언트 다운로드 실패. bucket={}, key={}, message={}", properties.getBucket(), objectKey, e.getMessage(), e);
            throw new BaseException(
                    ErrorResponseCode.INTERNAL_SERVER_ERROR,
                    "S3 파일을 다운로드하지 못했습니다. AWS 자격 증명 또는 네트워크 설정을 확인해 주세요.",
                    e
            );
        } catch (Exception e) {
            log.error("S3 파일 다운로드 실패. bucket={}, key={}", properties.getBucket(), objectKey, e);
            throw new BaseException(ErrorResponseCode.NOT_FOUND_ENDPOINT, "S3 파일을 다운로드하지 못했습니다.", e);
        }
    }

    @Override
    public void delete(String key) {
        if (!StringUtils.hasText(key)) {
            return;
        }

        String objectKey = toObjectKey(key);

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .build());
        } catch (AwsServiceException e) {
            log.warn(
                    "S3 파일 삭제 실패. bucket={}, key={}, statusCode={}, awsErrorCode={}, requestId={}, message={}",
                    properties.getBucket(),
                    objectKey,
                    e.statusCode(),
                    e.awsErrorDetails() == null ? null : e.awsErrorDetails().errorCode(),
                    e.requestId(),
                    e.awsErrorDetails() == null ? e.getMessage() : e.awsErrorDetails().errorMessage(),
                    e
            );
        } catch (SdkClientException e) {
            log.warn("S3 클라이언트 삭제 실패. bucket={}, key={}, message={}", properties.getBucket(), objectKey, e.getMessage(), e);
        } catch (Exception e) {
            log.warn("S3 파일 삭제 실패. bucket={}, key={}", properties.getBucket(), objectKey, e);
        }
    }

    @Override
    public URI createPresignedGetUri(String key, Duration expiration) {
        String objectKey = toObjectKey(key);

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            return URI.create(s3Presigner.presignGetObject(presignRequest).url().toString());
        } catch (Exception e) {
            log.error("S3 presigned URL 생성 실패. bucket={}, key={}", properties.getBucket(), objectKey, e);
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "S3 다운로드 URL 생성에 실패했습니다.", e);
        }
    }

    @Override
    public boolean supportsPresignedUrl() {
        return true;
    }

    private String toObjectKey(String key) {
        if (!StringUtils.hasText(key) || key.startsWith("/") || key.contains("..")) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "S3 객체 key가 올바르지 않습니다.");
        }

        String normalizedKey = key.replace('\\', '/');
        String prefix = normalizePrefix(properties.getPrefix());
        return prefix.isEmpty() ? normalizedKey : prefix + "/" + normalizedKey;
    }

    private String normalizePrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return "";
        }

        String normalized = prefix.trim().replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String extractSuffix(String key) {
        int dotIndex = key.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == key.length() - 1) {
            return ".tmp";
        }
        return key.substring(dotIndex);
    }
}
