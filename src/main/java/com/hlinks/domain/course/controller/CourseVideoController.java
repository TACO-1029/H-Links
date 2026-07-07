package com.hlinks.domain.course.controller;

import com.hlinks.domain.course.service.AdminCourseService;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class CourseVideoController {

    private final AdminCourseService adminCourseService;

    @GetMapping("/videos/courses/{courseId}/chapters/{chapterId}")
    public ResponseEntity<Resource> streamVideo(
            @PathVariable Long courseId,
            @PathVariable Long chapterId
    ) {
        Path videoPath = adminCourseService.resolveVideoPath(courseId, chapterId);

        if (!Files.exists(videoPath) || !Files.isReadable(videoPath)) {
            throw new BaseException(ErrorResponseCode.NOT_FOUND_ENDPOINT, "강의 영상을 찾을 수 없습니다.");
        }

        try {
            Resource resource = new UrlResource(videoPath.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "강의 영상 경로가 올바르지 않습니다.", e);
        }
    }

    @GetMapping("/images/courses/{courseId}/{fileName:.+}")
    public ResponseEntity<Resource> showThumbnail(
            @PathVariable Long courseId,
            @PathVariable String fileName
    ) {
        Path thumbnailPath = adminCourseService.resolveThumbnailPath(courseId, fileName);

        if (!Files.exists(thumbnailPath) || !Files.isReadable(thumbnailPath)) {
            throw new BaseException(ErrorResponseCode.NOT_FOUND_ENDPOINT, "강의 썸네일을 찾을 수 없습니다.");
        }

        try {
            Resource resource = new UrlResource(thumbnailPath.toUri());
            MediaType mediaType = Optional.ofNullable(Files.probeContentType(thumbnailPath))
                    .map(MediaType::parseMediaType)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);
        } catch (Exception e) {
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "강의 썸네일 경로가 올바르지 않습니다.", e);
        }
    }

    @GetMapping("/materials/courses/{courseId}/{fileName:.+}")
    public ResponseEntity<Resource> downloadCourseMaterial(
            @PathVariable Long courseId,
            @PathVariable String fileName
    ) {
        Path materialPath = adminCourseService.resolveCourseMaterialPath(courseId, fileName);

        if (!Files.exists(materialPath) || !Files.isReadable(materialPath)) {
            throw new BaseException(ErrorResponseCode.NOT_FOUND_ENDPOINT, "강의 자료를 찾을 수 없습니다.");
        }

        try {
            Resource resource = new UrlResource(materialPath.toUri());
            MediaType mediaType = Optional.ofNullable(Files.probeContentType(materialPath))
                    .map(MediaType::parseMediaType)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);
            ContentDisposition contentDisposition = ContentDisposition.attachment()
                    .filename(materialPath.getFileName().toString(), StandardCharsets.UTF_8)
                    .build();

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                    .body(resource);
        } catch (Exception e) {
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "강의 자료 경로가 올바르지 않습니다.", e);
        }
    }
}
