package com.hlinks.domain.career.controller;

import com.hlinks.domain.career.service.CareerService;
import com.hlinks.domain.course.dto.CourseListResponseDto;
import com.hlinks.global.response.SliceResponse;
import com.hlinks.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/career-path")
@RequiredArgsConstructor
public class CareerApiController {

    private final CareerService careerService;

    @GetMapping("/diagnoses/{diagnosisId}/recommendations")
    public ResponseEntity<SliceResponse<CourseListResponseDto>> getRecommendations(
            @PathVariable Long diagnosisId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(careerService.getRecommendationCourseSlice(
                userDetails.getUserId(),
                diagnosisId,
                page,
                size
        ));
    }
}
