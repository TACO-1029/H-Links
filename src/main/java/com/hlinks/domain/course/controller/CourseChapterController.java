package com.hlinks.domain.course.controller;

import com.hlinks.domain.course.dto.ChapterTranscriptResponse;
import com.hlinks.domain.course.service.CourseChapterTranscriptService;
import com.hlinks.global.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/course-chapters")
@RequiredArgsConstructor
public class CourseChapterController {

    private final CourseChapterTranscriptService courseChapterTranscriptService;

    @GetMapping("/{chapterId}/transcript")
    public SuccessResponse<ChapterTranscriptResponse> getTranscript(@PathVariable Long chapterId) {
        return SuccessResponse.from(courseChapterTranscriptService.getTranscript(chapterId));
    }
}
