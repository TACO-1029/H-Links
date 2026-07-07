package com.hlinks.domain.recommend.kcy.service;

import com.hlinks.domain.recommend.kcy.dto.*;
import com.hlinks.domain.recommend.kcy.exception.KcyErrorCode;
import com.hlinks.domain.recommend.kcy.mapper.KcyMapper;
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

    private static final int MIN_MCQ_FOR_EARLY_STOP = 5;
    private static final int MAX_MCQ_LIMIT = 9;
    private static final int SCORE_DIFF_THRESHOLD = 3;
    private static final int BASE_SCORE = 0;

    private final KcyMapper kcyMapper;

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
        
        // 조기 종료 조건: MCQ를 5문항 이상 풀었고 모든 축 격차가 20%p 이상으로 확실히 결정된 경우
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

        // 최대 문항 수 제한 도달 조건: MCQ를 6문항 다 푼 경우
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
                .nextQuestion(nextQ)
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
        // 검증 로직은 필요에 따라 유연하게 (Adaptive 에서는 개수가 가변적)
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

        KcyType resultType = score.toKcyType();
        int updatedCount = kcyMapper.updateUserKcyResult(userId, resultType.getCode());

        if (updatedCount != 1) {
            throw new BaseException(KcyErrorCode.KCY_RESULT_SAVE_FAILED);
        }
        return score;
    }

    private KcyScoreDto calculateCurrentScore(List<Long> optionIds, List<String> angerTypes, List<String> tetrisBlocks, Integer timeTaken, Integer fillRate) {
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
                score.addScore(type, 1);
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

    private List<TetrisBlockDto> generateBlocks(KcyScoreDto score) {
        List<TetrisBlockDto> blocks = new ArrayList<>();
        int pDiff = Math.abs(score.getPrompterScore() - score.getManualScore());
        int aDiff = Math.abs(score.getActionScore() - score.getOutlineScore());
        
        // Tiebreaker logic
        if (pDiff < SCORE_DIFF_THRESHOLD) {
            // Need Prompter vs Manual tiebreaker blocks
            blocks.add(TetrisBlockRegistry.AI_PROMPT_ENG.toDto());
            blocks.add(TetrisBlockRegistry.SWE_QA.toDto());
            blocks.add(TetrisBlockRegistry.AI_HARNESS.toDto());
            blocks.add(TetrisBlockRegistry.BE_JDBC.toDto());
        } else if (aDiff < SCORE_DIFF_THRESHOLD) {
            // Need Action vs Outline tiebreaker blocks
            blocks.add(TetrisBlockRegistry.INFRA_DOCKER.toDto());
            blocks.add(TetrisBlockRegistry.SWE_AGILE.toDto());
            blocks.add(TetrisBlockRegistry.AI_MULTI_AGENT.toDto());
            blocks.add(TetrisBlockRegistry.FE_REACT.toDto());
        } else {
            // Default preset
            blocks.add(TetrisBlockRegistry.INFRA_K8S.toDto());
            blocks.add(TetrisBlockRegistry.BE_SPRING.toDto());
            blocks.add(TetrisBlockRegistry.FE_VUE.toDto());
            blocks.add(TetrisBlockRegistry.SWE_TDD.toDto());
            blocks.add(TetrisBlockRegistry.AI_COPILOT.toDto());
        }
        return blocks;
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
}
