package com.hlinks.domain.recommend.kcy.service;

import com.hlinks.domain.recommend.kcy.dto.KcyQuestionDto;
import com.hlinks.domain.recommend.kcy.dto.KcyScoreDto;
import com.hlinks.domain.recommend.kcy.type.KcyType;

import java.util.List;

public interface KcyService {

    List<KcyQuestionDto> getQuestions();

    KcyScoreDto submit(Long userId, List<Long> selectedOptionIds);

    KcyType getResult(Long userId);
}
