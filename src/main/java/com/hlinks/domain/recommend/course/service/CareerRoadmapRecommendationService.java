package com.hlinks.domain.recommend.course.service;

import com.hlinks.domain.recommend.course.dto.CareerRoadmapResponse;
import com.hlinks.domain.recommend.course.dto.CourseRecommendationRequest;

public interface CareerRoadmapRecommendationService {

    CareerRoadmapResponse recommendRoadmapByLevelTest(CourseRecommendationRequest request);
}
