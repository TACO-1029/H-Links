package com.hlinks.domain.course.controller;

import com.hlinks.domain.course.service.AdminCourseService;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;
import com.hlinks.global.storage.DownloadedFile;
import com.hlinks.global.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class CourseVideoController {

    private final AdminCourseService adminCourseService;
    private final FileStorageService fileStorageService;

    @Value("${hlinks.storage.s3.presigned-url-expiration-minutes:10}")
    private long presignedUrlExpirationMinutes;

    @GetMapping("/videos/courses/{courseId}/chapters/{chapterId}")
    public ResponseEntity<?> streamVideo(
            @PathVariable Long courseId,
            @PathVariable Long chapterId
    ) {
        String videoKey = adminCourseService.resolveVideoKey(courseId, chapterId);
        if (fileStorageService.supportsPresignedUrl()) {
            return redirectToPresignedUrl(videoKey);
        }

        DownloadedFile downloadedFile = fileStorageService.download(videoKey);

        try {
            Resource resource = new UrlResource(downloadedFile.path().toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "강의 영상 경로가 올바르지 않습니다.", e);
        }
    }

    @GetMapping("/images/courses/{courseId}/{fileName:.+}")
    public ResponseEntity<?> showThumbnail(
            @PathVariable Long courseId,
            @PathVariable String fileName
    ) {
        String thumbnailKey = adminCourseService.resolveThumbnailKey(courseId, fileName);
        if (fileStorageService.supportsPresignedUrl()) {
            return redirectToPresignedUrl(thumbnailKey);
        }

        DownloadedFile downloadedFile = fileStorageService.download(thumbnailKey);

        try {
            Resource resource = new UrlResource(downloadedFile.path().toUri());
            MediaType mediaType = Optional.ofNullable(Files.probeContentType(downloadedFile.path()))
                    .map(MediaType::parseMediaType)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);
        } catch (Exception e) {
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "강의 썸네일 경로가 올바르지 않습니다.", e);
        }
    }

    @GetMapping("/images/course/{fileName:.+}")
    public ResponseEntity<Void> redirectLegacyThumbnail(
            @PathVariable String fileName
    ) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/images/common/hyundai_futurenet.webp"))
                .build();
    }

    @GetMapping("/materials/courses/{courseId}/{fileName:.+}")
    public ResponseEntity<?> downloadCourseMaterial(
            @PathVariable Long courseId,
            @PathVariable String fileName
    ) {
        String materialKey = adminCourseService.resolveCourseMaterialKey(courseId, fileName);
        if (fileStorageService.supportsPresignedUrl()) {
            return redirectToPresignedUrl(materialKey);
        }

        DownloadedFile downloadedFile = fileStorageService.download(materialKey);

        try {
            Path materialPath = downloadedFile.path();
            Resource resource = new UrlResource(materialPath.toUri());
            MediaType mediaType = Optional.ofNullable(Files.probeContentType(materialPath))
                    .map(MediaType::parseMediaType)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);
            ContentDisposition contentDisposition = ContentDisposition.attachment()
                    .filename(fileName, StandardCharsets.UTF_8)
                    .build();

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                    .body(resource);
        } catch (Exception e) {
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "강의 자료 경로가 올바르지 않습니다.", e);
        }
    }

    private ResponseEntity<Void> redirectToPresignedUrl(String key) {
        URI uri = fileStorageService.createPresignedGetUri(
                key,
                Duration.ofMinutes(Math.max(1, presignedUrlExpirationMinutes))
        );

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(uri)
                .build();
    }
}
