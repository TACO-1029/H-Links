package com.hlinks.domain.recommend.kcy.service;

import com.hlinks.domain.recommend.kcy.dto.KcyQuestionDto;
import com.hlinks.domain.recommend.kcy.type.KcyType;

import java.util.List;

public interface KcyService {

    List<KcyQuestionDto> getQuestions();

    KcyType submit(Long userId, List<Long> selectedOptionIds);

    KcyType getResult(Long userId);
}
