package com.hlinks.domain.recommend.kcy.service;

import com.hlinks.domain.recommend.kcy.dto.*;
import com.hlinks.domain.recommend.kcy.type.KcyType;

import java.util.List;

public interface KcyService {

    List<KcyQuestionDto> getQuestions();

    KcyAdaptiveResponse getNextAdaptiveQuestion(KcyAdaptiveRequest request);

    KcyScoreDto submit(Long userId, KcySubmitRequest request);

    KcyType getResult(Long userId);

    List<KcyPartnerRecommendationDto> getRecommendedPartners(Long userId);
}
