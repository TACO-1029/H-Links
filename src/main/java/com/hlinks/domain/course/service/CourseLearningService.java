package com.hlinks.domain.course.service;

import com.hlinks.domain.course.dto.LearningProgressResponseDto;
import com.hlinks.domain.course.dto.LearningProgressSaveRequest;
import com.hlinks.domain.course.dto.LearningProgressTargetDto;
import com.hlinks.domain.course.mapper.CourseMapper;
import com.hlinks.domain.course.type.LearningStatus;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseLearningService {

    private static final int MAX_PROGRESS_JUMP_SECONDS = 30;
    private static final String EVENT_START = "START";
    private static final String EVENT_PAUSE = "PAUSE";
    private static final String EVENT_EXIT = "EXIT";
    private static final String EVENT_COMPLETE = "COMPLETE";

    private final CourseService courseService;
    private final CourseMapper courseMapper;

    @Transactional
    public void startOnlineChapterLearning(Long courseId, Long chapterId, Long userId) {
        courseService.getOnlineChapterPage(courseId, chapterId, userId);

        Long courseLearningId = courseMapper.findCourseLearningId(userId, courseId);
        if (courseLearningId == null) {
            throw new BaseException(ErrorResponseCode.FORBIDDEN);
        }

        courseMapper.startCourseLearning(courseLearningId, LearningStatus.IN_PROGRESS.name());
        courseMapper.startChapterLearning(courseLearningId, chapterId, LearningStatus.IN_PROGRESS.name());

        LearningProgressTargetDto target = courseMapper.findLearningProgressTarget(userId, courseId, chapterId);
        if (target != null) {
            courseMapper.insertLearningLog(
                    target.getCourseLearningId(),
                    target.getChapterLearningId(),
                    userId,
                    courseId,
                    chapterId,
                    EVENT_START,
                    defaultNumber(target.getLastPlaySeconds()),
                    defaultNumber(target.getProgressRate())
            );
        }
    }

    public LearningProgressResponseDto getChapterProgress(Long courseId, Long chapterId, Long userId) {
        LearningProgressTargetDto target = courseMapper.findLearningProgressTarget(userId, courseId, chapterId);
        if (target == null) {
            throw new BaseException(ErrorResponseCode.FORBIDDEN);
        }

        return LearningProgressResponseDto.builder()
                .lastPlaySeconds(defaultNumber(target.getLastPlaySeconds()))
                .maxPlaySeconds(defaultNumber(target.getMaxPlaySeconds()))
                .progressRate(defaultNumber(target.getProgressRate()))
                .status(target.getStatus())
                .build();
    }

    @Transactional
    public void saveChapterProgress(
            Long courseId,
            Long chapterId,
            Long userId,
            LearningProgressSaveRequest request) {

        LearningProgressTargetDto target = courseMapper.findLearningProgressTarget(userId, courseId, chapterId);
        if (target == null) {
            throw new BaseException(ErrorResponseCode.FORBIDDEN);
        }

        int durationSeconds = resolveDurationSeconds(target, request);
        int requestedPlaySeconds = clampPlaySeconds(defaultNumber(request.getLastPlaySeconds()), durationSeconds);
        int previousMaxPlaySeconds = defaultNumber(target.getMaxPlaySeconds());
        int maxPlaySeconds = resolveMaxPlaySeconds(previousMaxPlaySeconds, requestedPlaySeconds);
        int lastPlaySeconds = resolveLastPlaySeconds(previousMaxPlaySeconds, requestedPlaySeconds, maxPlaySeconds);
        int progressRate = calculateProgressRate(maxPlaySeconds, durationSeconds);
        boolean alreadyCompleted = LearningStatus.COMPLETED.name().equals(target.getStatus());

        if (alreadyCompleted) {
            return;
        }

        courseMapper.updateChapterLearningProgress(
                target.getChapterLearningId(),
                lastPlaySeconds,
                maxPlaySeconds,
                progressRate
        );

        if (shouldInsertLearningLog(request)) {
            courseMapper.insertLearningLog(
                    target.getCourseLearningId(),
                    target.getChapterLearningId(),
                    userId,
                    courseId,
                    chapterId,
                    resolveEventType(request),
                    lastPlaySeconds,
                    progressRate
            );
        }

        if (Boolean.TRUE.equals(request.getFlush())) {
            courseMapper.updateCourseLearningProgress(target.getCourseLearningId(), courseId);
        }
    }

    private int resolveDurationSeconds(LearningProgressTargetDto target, LearningProgressSaveRequest request) {
        int targetDurationSeconds = defaultNumber(target.getDurationSeconds());
        if (targetDurationSeconds > 0) {
            return targetDurationSeconds;
        }
        return defaultNumber(request.getDurationSeconds());
    }

    private int clampPlaySeconds(int playSeconds, int durationSeconds) {
        int safePlaySeconds = Math.max(0, playSeconds);
        if (durationSeconds <= 0) {
            return safePlaySeconds;
        }
        return Math.min(safePlaySeconds, durationSeconds);
    }

    private int calculateProgressRate(int playSeconds, int durationSeconds) {
        if (durationSeconds <= 0) {
            return 0;
        }
        int progressRate = (int) Math.floor(playSeconds * 100.0 / durationSeconds);
        return Math.min(100, Math.max(0, progressRate));
    }

    private int resolveMaxPlaySeconds(int previousMaxPlaySeconds, int requestedPlaySeconds) {
        if (requestedPlaySeconds <= previousMaxPlaySeconds + MAX_PROGRESS_JUMP_SECONDS) {
            return Math.max(previousMaxPlaySeconds, requestedPlaySeconds);
        }
        return previousMaxPlaySeconds;
    }

    private int resolveLastPlaySeconds(int previousMaxPlaySeconds, int requestedPlaySeconds, int maxPlaySeconds) {
        if (requestedPlaySeconds <= previousMaxPlaySeconds + MAX_PROGRESS_JUMP_SECONDS) {
            return requestedPlaySeconds;
        }
        return maxPlaySeconds;
    }

    private boolean shouldInsertLearningLog(LearningProgressSaveRequest request) {
        String eventType = request.getEventType();
        return EVENT_PAUSE.equals(eventType) || EVENT_EXIT.equals(eventType) || EVENT_COMPLETE.equals(eventType);
    }

    private String resolveEventType(LearningProgressSaveRequest request) {
        String eventType = request.getEventType();
        if (EVENT_PAUSE.equals(eventType) || EVENT_EXIT.equals(eventType) || EVENT_COMPLETE.equals(eventType)) {
            return eventType;
        }
        return "PROGRESS";
    }

    private int defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }
}
