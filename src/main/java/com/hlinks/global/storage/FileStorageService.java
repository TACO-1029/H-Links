package com.hlinks.global.storage;

import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.Duration;

public interface FileStorageService {

    StoredFile upload(String key, MultipartFile file);

    DownloadedFile download(String key);

    void delete(String key);

    URI createPresignedGetUri(String key, Duration expiration);

    boolean supportsPresignedUrl();
}
