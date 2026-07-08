package com.hlinks.global.storage;

public record StoredFile(
        String key,
        String originalFileName,
        String contentType,
        long size
) {
}
