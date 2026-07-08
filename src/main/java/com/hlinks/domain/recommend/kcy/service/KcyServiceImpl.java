package com.hlinks.domain.recommend.kcy.service;

import com.hlinks.domain.competency.service.CompetencyScoreService;
import com.hlinks.domain.competency.type.CompetencyCalcType;
import com.hlinks.domain.recommend.kcy.dto.KcyMatchCandidateDto;
import com.hlinks.domain.recommend.kcy.dto.KcyOptionDto;
import com.hlinks.domain.recommend.kcy.dto.KcyPartnerRecommendationDto;
import com.hlinks.domain.recommend.kcy.dto.KcyQuestionDto;
import com.hlinks.domain.recommend.kcy.dto.KcyScoreDto;
import com.hlinks.domain.recommend.kcy.dto.*;
import com.hlinks.domain.recommend.kcy.exception.KcyErrorCode;
import com.hlinks.domain.recommend.kcy.mapper.KcyMapper;
import com.hlinks.domain.recommend.kcy.type.KcyCompatibilityPolicy;
import com.hlinks.domain.recommend.kcy.type.KcyMatchGrade;
import com.hlinks.domain.recommend.kcy.type.KcyType;
import com.hlinks.domain.recommend.kcy.type.TetrisBlockRegistry;
import com.hlinks.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KcyServiceImpl implements KcyService {

    private static final String REFERENCE_TYPE_KCY_TEST = "KCY_TEST";
    private static final int REQUIRED_QUESTION_COUNT = 11;
    private static final int RECOMMENDED_PARTNER_LIMIT = 3;
    private static final List<String> ANONYMOUS_NICKNAMES = List.of(
            "두더지", "고양이", "강아지", "수달", "토끼", "햄스터", "판다", "여우"
    );
    private static final int MIN_MCQ_FOR_EARLY_STOP = 5;
    private static final int MAX_MCQ_LIMIT = 9;
    private static final int SCORE_DIFF_THRESHOLD = 3;
    private static final int BASE_SCORE = 0;

    private final KcyMapper kcyMapper;
    private final CompetencyScoreService competencyScoreService;

    @Override
    public List<KcyQuestionDto> getQuestions() {
        List<KcyQuestionDto> questions = kcyMapper.findActiveQuestions();
        if (questions.isEmpty()) {
            throw new BaseException(KcyErrorCode.KCY_QUESTION_NOT_FOUND);
        }

        List<Long> questionIds = questions.stream().map(KcyQuestionDto::getKcyQuestionId).collect(Collectors.toList());
        List<KcyOptionDto> options = kcyMapper.findOptionsByQuestionIds(questionIds);

        Map<Long, KcyQuestionDto> questionMap = questions.stream()
                .collect(Collectors.toMap(KcyQuestionDto::getKcyQuestionId, q -> q, (a, b) -> a, LinkedHashMap::new));

        for (KcyOptionDto option : options) {
            KcyQuestionDto question = questionMap.get(option.getKcyQuestionId());
            if (question != null) {
                question.addOption(option);
            }
        }
        return questions;
    }

    @Override
    public KcyAdaptiveResponse getNextAdaptiveQuestion(KcyAdaptiveRequest request) {
        // 조건 1: 코드 리뷰는 끝났으나 테트리스 배포가 아직 완료되지 않은 경우 -> 즉시 테트리스 단계 개시
        if (request.getTetrisFinished() == null || !request.getTetrisFinished()) {
            List<TetrisBlockDto> allBlocks = Arrays.stream(TetrisBlockRegistry.values())
                    .map(TetrisBlockRegistry::toDto)
                    .collect(Collectors.toList());

            return KcyAdaptiveResponse.builder()
                    .status("TETRIS_PHASE")
                    .currentCount(0)
                    .totalPredictCount(MAX_MCQ_LIMIT)
                    .blocks(allBlocks)
                    .build();
        }

        // 조건 2: 테트리스가 배포되었거나 이미 MCQ 진행 중인 경우
        List<KcyQuestionDto> allQuestions = getQuestions();

        KcyScoreDto currentScore = calculateCurrentScore(
                request.getSelectedOptionIds(),
                request.getAngerScoreTypes(),
                request.getTiebreakerBlocks(),
                request.getTimeTaken(),
                request.getFillRate()
        );

        log.info("[KCY DEBUG] Current Scores - A/O(Action/Outline): {}/{}, W/D(Wide/Deep): {}/{}, I/C(Independent/Corporate): {}/{}, P/M(Prompter/Manual): {}/{}",
                currentScore.getActionScore(), currentScore.getOutlineScore(),
                currentScore.getWideScore(), currentScore.getDeepScore(),
                currentScore.getIndependentScore(), currentScore.getCorporateScore(),
                currentScore.getPrompterScore(), currentScore.getManualScore());

        int answeredCount = request.getSelectedOptionIds() != null ? request.getSelectedOptionIds().size() : 0;

        // 조기 종료 조건: MCQ를 5문항 이상 풀었고 최소 2개 이상의 축 성향이 40%/60% 비율 범위 밖으로 굳어진 경우
        // 단, 사용자가 우회를 희망하여 bypassEarlyStop = true 로 오면 건너뛴다.
        if (answeredCount >= MIN_MCQ_FOR_EARLY_STOP && isAllAxesDetermined(currentScore)) {
            Boolean bes = request.getBypassEarlyStop();
            if (bes == null || !bes) {
                return KcyAdaptiveResponse.builder()
                        .status("EARLY_STOP_PROMPT")
                        .currentCount(answeredCount)
                        .totalPredictCount(answeredCount)
                        .build();
            }
        }

        // 최대 문항 수 제한 도달 조건: MCQ를 9문항 다 푼 경우
        // 단, 사용자가 우회를 희망하여 bypassEarlyStop = true 로 오면 최대 11문항 풀을 다 풀 때까지 계속 진행
        if (answeredCount >= MAX_MCQ_LIMIT) {
            Boolean bes = request.getBypassEarlyStop();
            if (bes == null || !bes) {
                return KcyAdaptiveResponse.builder()
                        .status("EARLY_STOP_PROMPT")
                        .currentCount(answeredCount)
                        .totalPredictCount(answeredCount)
                        .build();
            }
        }

        // 안 푼 문제 중에서 Swing Range 가 가장 큰 문항 찾기
        List<Long> answeredQuestionIds = getAnsweredQuestionIds(allQuestions, request.getSelectedOptionIds());

        // 1. 가장 확정이 부족한(격차가 가장 작은) 축 선택
        String targetAxis = selectWeakestAxis(currentScore);

        // 2. 안 푼 문항들 중에서 해당 축에 대해 Swing Range가 가장 큰 문항 검색
        KcyQuestionDto nextQ = findBestAdaptiveQuestion(allQuestions, answeredQuestionIds, targetAxis);

        if (nextQ == null) {
             return KcyAdaptiveResponse.builder()
                    .status("FINAL_SUBMIT_PHASE")
                    .currentCount(answeredCount)
                    .totalPredictCount(answeredCount)
                    .build();
        }

        return KcyAdaptiveResponse.builder()
                .status("IN_PROGRESS_MCQ")
                .nextQuestion(KcyQuestionClientDto.from(nextQ))
                .currentCount(answeredCount + 1)
                .totalPredictCount(MAX_MCQ_LIMIT)
                .build();
    }

    private String selectWeakestAxis(KcyScoreDto score) {
        int aDiff = Math.abs(score.getActionScore() - score.getOutlineScore());
        int wDiff = Math.abs(score.getWideScore() - score.getDeepScore());
        int iDiff = Math.abs(score.getIndependentScore() - score.getCorporateScore());
        int pDiff = Math.abs(score.getPrompterScore() - score.getManualScore());

        int minDiff = Math.min(Math.min(aDiff, wDiff), Math.min(iDiff, pDiff));

        if (minDiff == aDiff) return "ACTION_OUTLINE";
        if (minDiff == wDiff) return "WIDE_DEEP";
        if (minDiff == iDiff) return "INDEPENDENT_CORPORATE";
        return "PROMPTER_MANUAL";
    }

    private KcyQuestionDto findBestAdaptiveQuestion(List<KcyQuestionDto> allQuestions, List<Long> answeredIds, String axis) {
        KcyQuestionDto bestQ = null;
        int maxSwing = -1;

        for (KcyQuestionDto q : allQuestions) {
            if (answeredIds.contains(q.getKcyQuestionId())) continue;

            int swing = calculateSwingRange(q, axis);
            if (swing > maxSwing) {
                maxSwing = swing;
                bestQ = q;
            }
        }
        return bestQ;
    }

    private int calculateSwingRange(KcyQuestionDto q, String axis) {
        if (q.getOptions() == null || q.getOptions().isEmpty()) return 0;

        int maxVal = Integer.MIN_VALUE;
        int minVal = Integer.MAX_VALUE;

        for (KcyOptionDto opt : q.getOptions()) {
            int val = 0;
            if ("ACTION_OUTLINE".equals(axis)) {
                val = opt.getActionScore() - opt.getOutlineScore();
            } else if ("WIDE_DEEP".equals(axis)) {
                val = opt.getWideScore() - opt.getDeepScore();
            } else if ("INDEPENDENT_CORPORATE".equals(axis)) {
                val = opt.getIndependentScore() - opt.getCorporateScore();
            } else if ("PROMPTER_MANUAL".equals(axis)) {
                val = opt.getPrompterScore() - opt.getManualScore();
            }

            if (val > maxVal) maxVal = val;
            if (val < minVal) minVal = val;
        }

        return maxVal - minVal;
    }

    @Override
    @Transactional
    public KcyScoreDto submit(Long userId, KcySubmitRequest request) {
        if (request.getSelectedOptionIds() == null) {
             request.setSelectedOptionIds(new ArrayList<>());
        }

        KcyScoreDto score = calculateCurrentScore(
            request.getSelectedOptionIds(),
            request.getAngerScoreTypes(),
            request.getTiebreakerBlocks(),
            request.getTimeTaken(),
            request.getFillRate()
        );

        // 50:50 동점 축 미세 보정 (Tiebreaker)
        applyTiebreakerAdjustments(score, request);

        KcyType resultType = score.toKcyType();
        int updatedCount = kcyMapper.updateUserKcyResult(userId, resultType.getCode());

        if (updatedCount != 1) {
            throw new BaseException(KcyErrorCode.KCY_RESULT_SAVE_FAILED);
        }
        competencyScoreService.applyActionScore(
                userId,
                CompetencyCalcType.KCY_TEST_TAKEN,
                REFERENCE_TYPE_KCY_TEST,
                userId
        );
        return score;
    }

    private KcyScoreDto calculateCurrentScore(List<Long> optionIds, List<String> angerTypes, List<String> tetrisBlocks, Integer timeTaken, Integer fillRate) {
        if (timeTaken != null && (timeTaken < 0)) {
            throw new BaseException(KcyErrorCode.KCY_INVALID_INPUT_VALUE, timeTaken.toString());
        }
        if (fillRate != null && (fillRate < 0 || fillRate > 100)) {
            throw new BaseException(KcyErrorCode.KCY_INVALID_INPUT_VALUE, fillRate.toString());
        }
        if (optionIds != null && !optionIds.isEmpty()) {
            List<KcyQuestionDto> activeQuestions = kcyMapper.findActiveQuestions();
            if (activeQuestions != null && !activeQuestions.isEmpty()) {
                List<Long> qIds = activeQuestions.stream()
                        .map(KcyQuestionDto::getKcyQuestionId)
                        .collect(Collectors.toList());
                List<KcyOptionDto> activeOptions = kcyMapper.findOptionsByQuestionIds(qIds);
                if (activeOptions != null) {
                    java.util.Set<Long> validOptionIds = activeOptions.stream()
                            .map(KcyOptionDto::getKcyOptionId)
                            .collect(Collectors.toSet());
                    for (Long optId : optionIds) {
                        if (optId == null || !validOptionIds.contains(optId)) {
                            throw new BaseException(KcyErrorCode.KCY_INVALID_INPUT_VALUE);
                        }
                    }
                }
            }
        }
        if (angerTypes != null) {
            Set<String> validAxes = Set.of("ACTION", "OUTLINE", "WIDE", "DEEP", "INDEPENDENT", "CORPORATE", "PROMPTER", "MANUAL");
            for (String type : angerTypes) {
                if (type == null || "".equals(type)) {
                    continue;
                }
                if (!validAxes.contains(type)) {
                    throw new BaseException(KcyErrorCode.KCY_INVALID_INPUT_VALUE);
                }
            }
        }
        if (tetrisBlocks != null) {
            for (String blockId : tetrisBlocks) {
                if (blockId == null || TetrisBlockRegistry.findById(blockId) == null) {
                    throw new BaseException(KcyErrorCode.KCY_INVALID_INPUT_VALUE);
                }
            }
        }

        KcyScoreDto score;
        if (optionIds != null && !optionIds.isEmpty()) {
            score = kcyMapper.sumScoresByOptionIds(optionIds);
        } else {
            score = new KcyScoreDto();
        }

        if (score == null) score = new KcyScoreDto();

        // [통계 보정] 40%/60% 비율제 하에서 0점 분모에 의한 극단적 쏠림 방지용 초기 Base점 적재
        score.addScore("ACTION", BASE_SCORE);
        score.addScore("OUTLINE", BASE_SCORE);
        score.addScore("WIDE", BASE_SCORE);
        score.addScore("DEEP", BASE_SCORE);
        score.addScore("INDEPENDENT", BASE_SCORE);
        score.addScore("CORPORATE", BASE_SCORE);
        score.addScore("PROMPTER", BASE_SCORE);
        score.addScore("MANUAL", BASE_SCORE);

        // 1. 하이라이터 훅 점수 추가 (MCQ 대비 가중치 미미하게 1점 부여)
        if (angerTypes != null) {
            for (String type : angerTypes) {
                if (type != null) {
                    score.addScore(type, 1);
                }
            }
        }

        // 2. 테트리스 블록 점수 추가 (MCQ 대비 가중치 미미하게 1점 부여)
        if (tetrisBlocks != null) {
            for (String blockId : tetrisBlocks) {
                TetrisBlockRegistry registry = TetrisBlockRegistry.findById(blockId);
                if (registry != null) {
                    if (registry.getTargetScoreAxis() != null) {
                        score.addScore(registry.getTargetScoreAxis(), 1);
                    }
                    // 블록 형상 종횡비를 기반으로 한 동적 wide/deep 1점 보조 판별 합산
                    applyBlockShapeScores(score, registry);
                }
            }
        }

        // 3. 테트리스 시간/충전율 점수 추가 (가중치 1점 부여)
        if (timeTaken != null) {
            if (timeTaken < 15) score.addScore("ACTION", 1);
            else if (timeTaken > 30) score.addScore("OUTLINE", 1);
        }

        if (fillRate != null) {
            if (fillRate >= 80) score.addScore("OUTLINE", 1);
            else if (fillRate <= 60) score.addScore("ACTION", 1);
        }

        return score;
    }

    private void applyBlockShapeScores(KcyScoreDto score, TetrisBlockRegistry block) {
        int width = block.getWidth();
        int height = block.getHeight();
        int diff = Math.abs(width - height);

        if (diff == 0) {
            // 정사각형 블록 -> WIDE 1점 반영
            score.addScore("WIDE", 1);
        } else if (diff >= 2) {
            // 가로세로 비율차가 큰 직사각형 블록 -> DEEP 1점 반영
            score.addScore("DEEP", 1);
        }
    }



    private boolean isAllAxesDetermined(KcyScoreDto score) {
        int determinedCount = 0;
        if (isAxisDetermined(score.getActionScore(), score.getOutlineScore())) determinedCount++;
        if (isAxisDetermined(score.getWideScore(), score.getDeepScore())) determinedCount++;
        if (isAxisDetermined(score.getIndependentScore(), score.getCorporateScore())) determinedCount++;
        if (isAxisDetermined(score.getPrompterScore(), score.getManualScore())) determinedCount++;
        return determinedCount >= 2;
    }

    private boolean isAxisDetermined(int a, int b) {
        if (a + b == 0) return false;
        double ratio = (double) a / (a + b);
        return ratio < 0.4 || ratio > 0.6;
    }

    private List<Long> getAnsweredQuestionIds(List<KcyQuestionDto> allQuestions, List<Long> selectedOptionIds) {
        if (selectedOptionIds == null || selectedOptionIds.isEmpty()) return new ArrayList<>();
        List<Long> answeredQIds = new ArrayList<>();
        for (KcyQuestionDto q : allQuestions) {
            for (KcyOptionDto opt : q.getOptions()) {
                if (selectedOptionIds.contains(opt.getKcyOptionId())) {
                    answeredQIds.add(q.getKcyQuestionId());
                    break;
                }
            }
        }
        return answeredQIds;
    }

    @Override
    public KcyType getResult(Long userId) {
        String kcyResult = kcyMapper.findKcyResultByUserId(userId);
        if (kcyResult == null || kcyResult.isBlank()) return null;
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

    private void applyTiebreakerAdjustments(KcyScoreDto score, KcySubmitRequest request) {
        // 1. ACTION vs OUTLINE
        if (score.getActionScore() == score.getOutlineScore()) {
            String winner = findEarlierChoiceWinner(request, "ACTION", "OUTLINE");
            if ("ACTION".equals(winner)) score.setActionScore(score.getActionScore() + 1);
            else if ("OUTLINE".equals(winner)) score.setOutlineScore(score.getOutlineScore() + 1);
        }
        // 2. WIDE vs DEEP
        if (score.getWideScore() == score.getDeepScore()) {
            String winner = findEarlierChoiceWinner(request, "WIDE", "DEEP");
            if ("WIDE".equals(winner)) score.setWideScore(score.getWideScore() + 1);
            else if ("DEEP".equals(winner)) score.setDeepScore(score.getDeepScore() + 1);
        }
        // 3. INDEPENDENT vs CORPORATE
        if (score.getIndependentScore() == score.getCorporateScore()) {
            String winner = findEarlierChoiceWinner(request, "INDEPENDENT", "CORPORATE");
            if ("INDEPENDENT".equals(winner)) score.setIndependentScore(score.getIndependentScore() + 1);
            else if ("CORPORATE".equals(winner)) score.setCorporateScore(score.getCorporateScore() + 1);
        }
        // 4. PROMPTER vs MANUAL
        if (score.getPrompterScore() == score.getManualScore()) {
            String winner = findEarlierChoiceWinner(request, "PROMPTER", "MANUAL");
            if ("PROMPTER".equals(winner)) score.setPrompterScore(score.getPrompterScore() + 1);
            else if ("MANUAL".equals(winner)) score.setManualScore(score.getManualScore() + 1);
        }
    }

    private String findEarlierChoiceWinner(KcySubmitRequest request, String axisA, String axisB) {
        // 기준 1: 하이라이팅 훅(angerScoreTypes)에서 더 많이 나타난 축 확인
        if (request.getAngerScoreTypes() != null) {
            long countA = request.getAngerScoreTypes().stream().filter(axisA::equalsIgnoreCase).count();
            long countB = request.getAngerScoreTypes().stream().filter(axisB::equalsIgnoreCase).count();
            if (countA > countB) return axisA;
            if (countB > countA) return axisB;
        }

        // 기준 2: 사용자가 푼 MCQ 중 가장 먼저 나온 선택지들의 가중치를 바탕으로 판별
        if (request.getSelectedOptionIds() != null && !request.getSelectedOptionIds().isEmpty()) {
            List<KcyQuestionDto> activeQuestions = kcyMapper.findActiveQuestions();
            if (activeQuestions != null && !activeQuestions.isEmpty()) {
                List<Long> qIds = activeQuestions.stream()
                        .map(KcyQuestionDto::getKcyQuestionId)
                        .collect(Collectors.toList());
                List<KcyOptionDto> activeOptions = kcyMapper.findOptionsByQuestionIds(qIds);
                if (activeOptions != null) {
                    Map<Long, KcyOptionDto> optionMap = activeOptions.stream()
                            .collect(Collectors.toMap(KcyOptionDto::getKcyOptionId, opt -> opt));
                    for (Long optId : request.getSelectedOptionIds()) {
                        KcyOptionDto opt = optionMap.get(optId);
                        if (opt != null) {
                            int valA = getOptionAxisScore(opt, axisA);
                            int valB = getOptionAxisScore(opt, axisB);
                            if (valA > valB) return axisA;
                            if (valB > valA) return axisB;
                        }
                    }
                }
            }
        }
        return null;
    }

    private int getOptionAxisScore(KcyOptionDto opt, String axis) {
        return switch (axis.toUpperCase()) {
            case "ACTION" -> opt.getActionScore();
            case "OUTLINE" -> opt.getOutlineScore();
            case "WIDE" -> opt.getWideScore();
            case "DEEP" -> opt.getDeepScore();
            case "INDEPENDENT" -> opt.getIndependentScore();
            case "CORPORATE" -> opt.getCorporateScore();
            case "PROMPTER" -> opt.getPrompterScore();
            case "MANUAL" -> opt.getManualScore();
            default -> 0;
        };
    }
}
