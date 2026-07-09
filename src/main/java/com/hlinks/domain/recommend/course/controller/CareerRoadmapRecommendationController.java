package com.hlinks.domain.recommend.course.controller;

import com.hlinks.domain.recommend.course.dto.CareerRoadmapResponse;
import com.hlinks.domain.recommend.course.dto.CourseRecommendationRequest;
import com.hlinks.domain.recommend.course.service.CareerRoadmapRecommendationService;
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
@RequestMapping("/api/recommendations")
public class CareerRoadmapRecommendationController {

    private final CareerRoadmapRecommendationService careerRoadmapRecommendationService;

    @PostMapping("/career-roadmap")
    public ResponseEntity<SuccessResponse<CareerRoadmapResponse>> recommendRoadmapByLevelTest(
            @Valid @RequestBody CourseRecommendationRequest request
    ) {
        CareerRoadmapResponse response = careerRoadmapRecommendationService.recommendRoadmapByLevelTest(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.from(response));
    }
}
