package com.hlinks.domain.interest.service;

import com.hlinks.domain.interest.dto.InterestDto;
import com.hlinks.domain.interest.exception.InterestErrorCode;
import com.hlinks.domain.interest.mapper.InterestMapper;
import com.hlinks.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterestServiceImpl implements InterestService {

    private static final int MAX_INTEREST_COUNT = 5;
    private final InterestMapper interestMapper;

    @Override
    public List<InterestDto> getAllInterests() {
        List<InterestDto> interests = interestMapper.findAllActiveInterests();

        if (interests.isEmpty()) {
            throw new BaseException(InterestErrorCode.INTEREST_NOT_FOUND);
        }

        return interests;
    }

    @Override
    public List<InterestDto> getUserInterests(Long userId) {
        return interestMapper.findInterestsByUserId(userId);
    }

    @Override
    public boolean hasInterests(Long userId) {
        return interestMapper.countInterestsByUserId(userId) > 0;
    }

    @Override
    @Transactional
    public void saveUserInterests(Long userId, List<Long> skillIds) {
        List<Long> distinctSkillIds = normalizeSkillIds(skillIds);
        validateSkillIds(distinctSkillIds);

        interestMapper.deleteInterestsByUserId(userId);

        for (Long skillId : distinctSkillIds) {
            interestMapper.insertUserInterest(userId, skillId);
        }
    }

    private List<Long> normalizeSkillIds(List<Long> skillIds) {
        if (skillIds == null) {
            return List.of();
        }

        return new LinkedHashSet<>(skillIds).stream()
                .toList();
    }

    private void validateSkillIds(List<Long> skillIds) {
        if (skillIds.isEmpty()) {
            throw new BaseException(InterestErrorCode.INTEREST_REQUIRED);
        }

        if (skillIds.size() > MAX_INTEREST_COUNT) {
            throw new BaseException(InterestErrorCode.INTEREST_TOO_MANY_SELECTED);
        }
    }
}
