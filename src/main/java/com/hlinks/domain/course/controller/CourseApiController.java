package com.hlinks.domain.course.controller;

import com.hlinks.domain.course.dto.CourseListResponseDto;
import com.hlinks.domain.course.service.CourseService;
import com.hlinks.global.response.SliceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CourseApiController {

    private final CourseService courseService;

    @GetMapping("/courses")
    public ResponseEntity<SliceResponse<CourseListResponseDto>> getCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) List<Long> skillIds,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String courseType,
            @RequestParam(required = false) List<String> courseTypes,
            @RequestParam(required = false) String categoryType,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        List<Long> resolvedSkillIds = resolveSkillIds(categoryId, skillIds);
        List<String> resolvedCourseTypes = resolveCourseTypes(courseType, courseTypes);
        return ResponseEntity.ok(courseService.getCourseSlice(
                categoryType,
                resolvedCourseTypes,
                resolvedSkillIds,
                keyword,
                difficulty,
                null,
                sort,
                page,
                size
        ));
    }

    @GetMapping("/career-high/courses")
    public ResponseEntity<SliceResponse<CourseListResponseDto>> getCareerHighCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean availableOnly
    ) {
        return ResponseEntity.ok(courseService.getCareerHighCourseSlice(
                keyword,
                categoryId,
                availableOnly,
                page,
                size
        ));
    }

    private List<Long> resolveSkillIds(Long categoryId, List<Long> skillIds) {
        if (skillIds != null && !skillIds.isEmpty()) {
            return skillIds;
        }
        if (categoryId != null) {
            return List.of(categoryId);
        }
        return List.of();
    }

    private List<String> resolveCourseTypes(String courseType, List<String> courseTypes) {
        if (courseTypes != null && !courseTypes.isEmpty()) {
            return courseTypes;
        }
        if (courseType != null && !courseType.isBlank()) {
            return List.of(courseType);
        }
        return List.of();
    }
}
