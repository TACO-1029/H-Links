package com.hlinks.global.storage;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public record DownloadedFile(Path path, boolean temporary) implements AutoCloseable {

    @Override
    public void close() {
        if (!temporary || path == null) {
            return;
        }

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("임시 다운로드 파일 삭제 실패. path={}", path, e);
        }
    }
}
