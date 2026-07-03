package com.hlinks.domain.interest.service;

import com.hlinks.domain.interest.dto.InterestDto;

import java.util.List;

public interface InterestService {

    List<InterestDto> getAllInterests();

    List<InterestDto> getUserInterests(Long userId);

    boolean hasInterests(Long userId);

    void saveUserInterests(Long userId, List<Long> skillIds);
}
