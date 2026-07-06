package com.hlinks.domain.recommend.course.service;

import com.hlinks.domain.recommend.course.dto.CourseRecommendationRequest;
import com.hlinks.domain.recommend.course.dto.CourseRecommendationResponse;

public interface CourseRecommendationService {

    CourseRecommendationResponse recommendByLevelTest(CourseRecommendationRequest request);
}
