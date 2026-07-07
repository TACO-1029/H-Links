package com.hlinks.global.storage;

import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

@Service
@ConditionalOnProperty(name = "hlinks.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    @Value("${file.upload.root:./storage/uploads}")
    private String uploadRoot;

    @Override
    public StoredFile upload(String key, MultipartFile file) {
        Path targetPath = resolveSafePath(key);

        try {
            Files.createDirectories(targetPath.getParent());

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return new StoredFile(key, file.getOriginalFilename(), file.getContentType(), file.getSize());
        } catch (IOException e) {
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "파일 저장에 실패했습니다.", e);
        }
    }

    @Override
    public DownloadedFile download(String key) {
        Path path = resolveSafePath(key);

        if (!Files.exists(path) || !Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw new BaseException(ErrorResponseCode.NOT_FOUND_ENDPOINT, "파일을 찾을 수 없습니다.");
        }

        return new DownloadedFile(path, false);
    }

    @Override
    public void delete(String key) {
        if (!StringUtils.hasText(key)) {
            return;
        }

        try {
            Files.deleteIfExists(resolveSafePath(key));
        } catch (IOException ignored) {
        }
    }

    @Override
    public URI createPresignedGetUri(String key, Duration expiration) {
        throw new UnsupportedOperationException("로컬 스토리지는 presigned URL을 지원하지 않습니다.");
    }

    @Override
    public boolean supportsPresignedUrl() {
        return false;
    }

    private Path resolveSafePath(String key) {
        if (!StringUtils.hasText(key)) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "파일 경로가 올바르지 않습니다.");
        }

        Path root = Path.of(uploadRoot).toAbsolutePath().normalize();
        Path targetPath = root.resolve(key.replace('\\', '/')).normalize();

        if (!targetPath.startsWith(root)) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "파일 경로가 올바르지 않습니다.");
        }

        return targetPath;
    }
}
