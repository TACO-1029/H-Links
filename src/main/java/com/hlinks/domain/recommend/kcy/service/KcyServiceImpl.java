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

    private static final int MIN_QUESTIONS_FOR_EARLY_STOP = 6;
    private static final int MAX_QUESTIONS = 9;
    private static final int SCORE_DIFF_THRESHOLD = 3;

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
        List<KcyQuestionDto> allQuestions = getQuestions();
        
        KcyScoreDto currentScore = calculateCurrentScore(request.getSelectedOptionIds(), request.getAngerScoreTypes(), null, null, null);
        
        log.info("[KCY DEBUG] Current Scores - A/O(Action/Outline): {}/{}, W/D(Wide/Deep): {}/{}, I/C(Independent/Corporate): {}/{}, P/M(Prompter/Manual): {}/{}",
                currentScore.getActionScore(), currentScore.getOutlineScore(),
                currentScore.getWideScore(), currentScore.getDeepScore(),
                currentScore.getIndependentScore(), currentScore.getCorporateScore(),
                currentScore.getPrompterScore(), currentScore.getManualScore());
        
        int answeredCount = request.getSelectedOptionIds() != null ? request.getSelectedOptionIds().size() : 0;
        
        // 조기 종료 조건: 6문항 이상 풀었고 모든 축 격차가 3점 이상
        if (answeredCount >= MIN_QUESTIONS_FOR_EARLY_STOP && isAllAxesDetermined(currentScore)) {
            return KcyAdaptiveResponse.builder()
                    .status("TETRIS_PHASE") // 조기 종료 시에도 테트리스는 무조건 노출
                    .currentCount(answeredCount)
                    .totalPredictCount(answeredCount)
                    .blocks(generateBlocks(currentScore))
                    .build();
        }

        if (answeredCount >= MAX_QUESTIONS) {
            return KcyAdaptiveResponse.builder()
                    .status("TETRIS_PHASE")
                    .currentCount(answeredCount)
                    .totalPredictCount(answeredCount)
                    .blocks(generateBlocks(currentScore))
                    .build();
        }

        // 안 푼 문제 중에서 Swing Range 가 가장 큰 문항 찾기
        List<Long> answeredQuestionIds = getAnsweredQuestionIds(allQuestions, request.getSelectedOptionIds());
        
        // 1. 가장 확정이 부족한(격차가 가장 작은) 축 선택
        String targetAxis = selectWeakestAxis(currentScore);
        
        // 2. 안 푼 문항들 중에서 해당 축에 대해 Swing Range가 가장 큰 문항 검색
        KcyQuestionDto nextQ = findBestAdaptiveQuestion(allQuestions, answeredQuestionIds, targetAxis);

        if (nextQ == null) {
             return KcyAdaptiveResponse.builder()
                    .status("TETRIS_PHASE")
                    .currentCount(answeredCount)
                    .totalPredictCount(answeredCount)
                    .blocks(generateBlocks(currentScore))
                    .build();
        }

        return KcyAdaptiveResponse.builder()
                .status("IN_PROGRESS_MCQ")
                .nextQuestion(nextQ)
                .currentCount(answeredCount + 1)
                .totalPredictCount(MAX_QUESTIONS)
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

        // 1. 하이라이터 훅 점수 추가
        if (angerTypes != null) {
            for (String type : angerTypes) {
                score.addScore(type, 2);
            }
        }

        // 2. 테트리스 블록 점수 추가
        if (tetrisBlocks != null) {
            for (String blockId : tetrisBlocks) {
                TetrisBlockRegistry registry = TetrisBlockRegistry.findById(blockId);
                if (registry != null && registry.getTargetScoreAxis() != null) {
                    score.addScore(registry.getTargetScoreAxis(), 2);
                }
            }
        }

        // 3. 테트리스 시간/충전율 점수 추가
        if (timeTaken != null) {
            if (timeTaken < 15) score.addScore("ACTION", 2);
            else if (timeTaken > 30) score.addScore("OUTLINE", 2);
        }

        if (fillRate != null) {
            if (fillRate >= 80) score.addScore("OUTLINE", 2);
            else if (fillRate <= 60) score.addScore("ACTION", 2);
        }

        return score;
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
        return Math.abs(score.getActionScore() - score.getOutlineScore()) >= SCORE_DIFF_THRESHOLD &&
               Math.abs(score.getWideScore() - score.getDeepScore()) >= SCORE_DIFF_THRESHOLD &&
               Math.abs(score.getIndependentScore() - score.getCorporateScore()) >= SCORE_DIFF_THRESHOLD &&
               Math.abs(score.getPrompterScore() - score.getManualScore()) >= SCORE_DIFF_THRESHOLD;
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
