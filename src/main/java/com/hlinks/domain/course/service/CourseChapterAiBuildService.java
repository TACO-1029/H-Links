package com.hlinks.domain.course.service;

import com.hlinks.domain.course.ai.dto.CourseSummaryGenerateResponse;
import com.hlinks.domain.course.entity.CourseChapter;
import com.hlinks.domain.course.mapper.CourseChapterMapper;
import com.hlinks.domain.quiz.ffmpeg.FfmpegService;
import com.hlinks.domain.quiz.service.QuizGenerateService;
import com.hlinks.domain.quiz.stt.SttService;
import com.hlinks.domain.quiz.type.QuizBuildStatus;
import com.hlinks.global.storage.DownloadedFile;
import com.hlinks.global.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseChapterAiBuildService {

    private static final int DEFAULT_QUIZ_COUNT = 3;
    private static final String DEFAULT_DIFFICULTY = "MEDIUM";

    private final CourseChapterMapper courseChapterMapper;
    private final FfmpegService ffmpegService;
    private final SttService sttService;
    private final CourseChapterSummaryService courseChapterSummaryService;
    private final QuizGenerateService quizGenerateService;
    private final FileStorageService fileStorageService;

    public void buildForChapter(Long chapterId) {
        int claimedCount = courseChapterMapper.updateQuizBuildStatusIfPending(
                chapterId,
                QuizBuildStatus.PROCESSING
        );

        if (claimedCount == 0) {
            log.info("챕터 AI 빌드 선점 생략. chapterId={}", chapterId);
            return;
        }

        DownloadedFile downloadedVideo = null;
        Path audioPath = null;

        try {
            CourseChapter chapter = findChapter(chapterId);

            if (chapter.getVideoPath() == null || chapter.getVideoPath().isBlank()) {
                throw new IllegalStateException("챕터 영상 경로가 없습니다. chapterId=" + chapterId);
            }

            downloadedVideo = fileStorageService.download(chapter.getVideoPath());
            Path videoPath = downloadedVideo.path();
            validateResolvedVideoPath(chapterId, videoPath);
            audioPath = ffmpegService.convertVideoToMp3(videoPath);

            String transcriptText = sttService.transcribe(audioPath);
            courseChapterMapper.updateTranscriptText(chapterId, transcriptText);

            CourseSummaryGenerateResponse summaryResponse = courseChapterSummaryService.generateSummary(chapterId);
            courseChapterSummaryService.saveSummaryText(chapterId, summaryResponse.getSummaryText());

            courseChapterSummaryService.saveChapterSkills(chapterId, summaryResponse.getSkills());

            quizGenerateService.generateAndSaveQuizzes(
                    chapterId,
                    DEFAULT_QUIZ_COUNT,
                    DEFAULT_DIFFICULTY
            );

            courseChapterMapper.updateQuizBuildStatus(chapterId, QuizBuildStatus.COMPLETED);
        } catch (Exception e) {
            log.error("챕터 AI 빌드 실패. chapterId={}", chapterId, e);
            updateFailedStatus(chapterId);
        } finally {
            deleteAudioFile(audioPath);
            closeDownloadedVideo(downloadedVideo);
        }
    }

    private CourseChapter findChapter(Long chapterId) {
        CourseChapter chapter = courseChapterMapper.findById(chapterId);

        if (chapter == null) {
            throw new IllegalStateException("존재하지 않는 챕터입니다. chapterId=" + chapterId);
        }

        return chapter;
    }

    private void validateResolvedVideoPath(Long chapterId, Path videoPath) {
        if (videoPath == null || !Files.exists(videoPath) || !Files.isRegularFile(videoPath) || !Files.isReadable(videoPath)) {
            throw new IllegalStateException(
                    "챕터 영상 파일을 찾을 수 없거나 읽을 수 없습니다. chapterId=" + chapterId + ", path=" + videoPath
            );
        }
    }

    private void updateFailedStatus(Long chapterId) {
        try {
            courseChapterMapper.updateQuizBuildStatus(chapterId, QuizBuildStatus.FAILED);
        } catch (Exception updateException) {
            log.warn("챕터 AI 빌드 실패 상태 저장 실패. chapterId={}", chapterId, updateException);
        }
    }

    private void deleteAudioFile(Path audioPath) {
        if (audioPath == null) {
            return;
        }

        try {
            ffmpegService.deleteIfExists(audioPath);
        } catch (Exception e) {
            log.warn("챕터 AI 빌드 임시 오디오 파일 삭제 실패. path={}", audioPath, e);
        }
    }

    private void closeDownloadedVideo(DownloadedFile downloadedVideo) {
        if (downloadedVideo == null) {
            return;
        }

        downloadedVideo.close();
    }
}
