package com.hlinks.domain.recommend.course.controller;

import com.hlinks.domain.recommend.course.dto.CourseRecommendationRequest;
import com.hlinks.domain.recommend.course.dto.CourseRecommendationResponse;
import com.hlinks.domain.recommend.course.service.CourseRecommendationService;
import com.hlinks.global.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations/courses")
public class CourseRecommendationController {

    private final CourseRecommendationService courseRecommendationService;

    @PostMapping("/level-test")
    public ResponseEntity<SuccessResponse<CourseRecommendationResponse>> recommendByLevelTest(
            @Valid @RequestBody CourseRecommendationRequest request
    ) {
        CourseRecommendationResponse response = courseRecommendationService.recommendByLevelTest(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.from(response));
    }
}
