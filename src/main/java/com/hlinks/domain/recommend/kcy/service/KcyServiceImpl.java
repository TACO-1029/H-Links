package com.hlinks.domain.recommend.kcy.service;

import com.hlinks.domain.recommend.kcy.dto.KcyMatchCandidateDto;
import com.hlinks.domain.recommend.kcy.dto.KcyOptionDto;
import com.hlinks.domain.recommend.kcy.dto.KcyPartnerRecommendationDto;
import com.hlinks.domain.recommend.kcy.dto.KcyQuestionDto;
import com.hlinks.domain.recommend.kcy.dto.KcyScoreDto;
import com.hlinks.domain.recommend.kcy.exception.KcyErrorCode;
import com.hlinks.domain.recommend.kcy.mapper.KcyMapper;
import com.hlinks.domain.recommend.kcy.type.KcyCompatibilityPolicy;
import com.hlinks.domain.recommend.kcy.type.KcyMatchGrade;
import com.hlinks.domain.recommend.kcy.type.KcyType;
import com.hlinks.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KcyServiceImpl implements KcyService {

    private static final int REQUIRED_QUESTION_COUNT = 11;
    private static final int RECOMMENDED_PARTNER_LIMIT = 3;
    private static final List<String> ANONYMOUS_NICKNAMES = List.of(
            "두더지", "고양이", "강아지", "수달", "토끼", "햄스터", "판다", "여우"
    );

    private final KcyMapper kcyMapper;
    // DB에서 따로 조회한 문항과 선택지를 화면에서 쓰기 좋은 구조로 다시 조립하는 메서드.
    @Override
    public List<KcyQuestionDto> getQuestions() {
        // kcy 테스트 질문을 가져오고
        List<KcyQuestionDto> questions = kcyMapper.findActiveQuestions();

        // 비어있다면 예외
        if (questions.isEmpty()) {
            throw new BaseException(KcyErrorCode.KCY_QUESTION_NOT_FOUND);
        }
        /* 1. stream을 활용한 방법입니다.
        List<Long> questionIds = questions.stream()
                .map(KcyQuestionDto::getKcyQuestionId)
                .toList();
        */
        List<Long> questionIds = new ArrayList<>();
        for (KcyQuestionDto question : questions) {
            questionIds.add(question.getKcyQuestionId());
        }
        // 질문 리스트를 넘겨주어서 질문에 맞는 문항리스트를 가져옵니다.
        List<KcyOptionDto> options = kcyMapper.findOptionsByQuestionIds(questionIds);

        Map<Long, KcyQuestionDto> questionMap = new LinkedHashMap<>();
        // 질문ID -> 질문 객체로 갈 수 있도록 변환합니다.
        for (KcyQuestionDto question : questions) {
            questionMap.put(question.getKcyQuestionId(), question);
        }

        // 문항들의 리스트를 돌면서 문항을 꺼내봅니다.
        for (KcyOptionDto option : options) {
            // 문항의 부모격인 질문ID를 꺼내와서 질문을 담은 Map에서 get합니다. (거의 바로 꺼내와서 무한 검색보다 성능 좋음)
            KcyQuestionDto question = questionMap.get(option.getKcyQuestionId());
            // 비어있지 않다면 질문 리스트에 추가
            if (question != null) {
                question.addOption(option);
            }
        }

        return questions;
    }

    @Override
    @Transactional
    public KcyScoreDto submit(Long userId, List<Long> selectedOptionIds) {
        // 먼저 검증합니다
        validateSelectedOptions(selectedOptionIds);
        // 점수 계산 로직을 타고와서
        KcyScoreDto score = kcyMapper.sumScoresByOptionIds(selectedOptionIds);

        if (score == null) {
            throw new BaseException(KcyErrorCode.KCY_SCORE_CALCULATION_FAILED);
        }
        // 점수에 알맞는 타입을 가져옵니다.
        KcyType resultType = score.toKcyType();
        // KCY 타입 업데이트를 하고 횟수 증가합니다.
        int updatedCount = kcyMapper.updateUserKcyResult(userId, resultType.getCode());

        if (updatedCount != 1) {
            throw new BaseException(KcyErrorCode.KCY_RESULT_SAVE_FAILED);
        }
        // 그 후 결과 반환
        return score;
    }
    // 예외 없는지 검사하는 함수입니다
    private void validateSelectedOptions(List<Long> selectedOptionIds) {
        if (selectedOptionIds == null || selectedOptionIds.isEmpty()) {
            throw new BaseException(KcyErrorCode.KCY_ANSWER_REQUIRED);
        }

        if (selectedOptionIds.size() != REQUIRED_QUESTION_COUNT) {
            throw new BaseException(KcyErrorCode.KCY_INVALID_ANSWER_COUNT);
        }

        int distinctQuestionCount = kcyMapper.countDistinctQuestionsByOptionIds(selectedOptionIds);

        if (distinctQuestionCount != REQUIRED_QUESTION_COUNT) {
            throw new BaseException(KcyErrorCode.KCY_INVALID_ANSWER_DUPLICATED);
        }
    }
    // id를 가지고 kcy 결과를 가져오는 서비스 메서드입니다.
    @Override
    public KcyType getResult(Long userId) {
        String kcyResult = kcyMapper.findKcyResultByUserId(userId);

        if (kcyResult == null || kcyResult.isBlank()) {
            return null;
        }

        return KcyType.from(kcyResult);
    }

    @Override
    public List<KcyPartnerRecommendationDto> getRecommendedPartners(Long userId) {
        KcyType myType = getResult(userId);

        if (myType == null) {
            return List.of();
        }

        return kcyMapper.findKcyMatchCandidates(userId).stream()
                .map(candidate -> toPartnerRecommendation(myType, candidate))
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparingInt(KcyPartnerRecommendationDto::getScore).reversed()
                        .thenComparing(KcyPartnerRecommendationDto::getUserId, Comparator.reverseOrder()))
                .limit(RECOMMENDED_PARTNER_LIMIT)
                .toList();
    }

    private KcyPartnerRecommendationDto toPartnerRecommendation(KcyType myType, KcyMatchCandidateDto candidate) {
        KcyType partnerType;

        try {
            partnerType = KcyType.from(candidate.getKcyResult());
        } catch (BaseException e) {
            return null;
        }

        KcyMatchGrade grade = KcyCompatibilityPolicy.gradeOf(myType, partnerType);

        return KcyPartnerRecommendationDto.builder()
                .userId(candidate.getUserId())
                .name(candidate.getName())
                .displayName(toAnonymousName(candidate.getName(), candidate.getUserId()))
                .departmentName(candidate.getDepartmentName())
                .jobName(candidate.getJobName())
                .positionName(candidate.getPositionName())
                .kcyCode(partnerType.getCode())
                .kcyTitle(partnerType.getTitle())
                .grade(grade.name())
                .gradeLabel(grade.getLabel())
                .score(grade.getScore())
                .reason(KcyCompatibilityPolicy.reasonOf(myType, partnerType, grade))
                .build();
    }

    private String toAnonymousName(String name, Long userId) {
        String familyName = resolveFamilyName(name);
        int nicknameIndex = Math.floorMod(Objects.hashCode(userId), ANONYMOUS_NICKNAMES.size());

        return familyName + ANONYMOUS_NICKNAMES.get(nicknameIndex);
    }

    private String resolveFamilyName(String name) {
        if (name == null || name.isBlank()) {
            return "동료";
        }

        String trimmedName = name.trim();
        String[] nameParts = trimmedName.split("\\s+");

        if (nameParts.length > 1) {
            return nameParts[nameParts.length - 1];
        }

        return trimmedName.substring(0, 1);
    }
}
