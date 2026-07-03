package com.hlinks.domain.course.controller;

import com.hlinks.domain.course.service.AdminCourseService;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

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
}
