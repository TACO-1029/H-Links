package com.hlinks.domain.quiz.service;

import com.hlinks.domain.course.entity.CourseChapter;
import com.hlinks.domain.course.mapper.CourseChapterMapper;
import com.hlinks.domain.quiz.ffmpeg.FfmpegService;
import com.hlinks.domain.quiz.stt.SttService;
import com.hlinks.domain.quiz.type.QuizBuildStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizBuildService {

    private static final int DEFAULT_QUIZ_COUNT = 10;
    private static final String DEFAULT_DIFFICULTY = "BASIC";

    private final CourseChapterMapper courseChapterMapper;
    private final FfmpegService ffmpegService;
    private final SttService sttService;
    private final QuizGenerateService quizGenerateService;

    public void buildQuizForChapter(Long chapterId) {
        CourseChapter chapter = courseChapterMapper.findById(chapterId);

        if (chapter == null) {
            return;
        }

        Path audioPath = null;

        try {
            // pending -> processing
            courseChapterMapper.updateQuizBuildStatus(chapterId, QuizBuildStatus.PROCESSING);

            if (chapter.getVideoPath() == null || chapter.getVideoPath().isBlank()) {
                throw new IllegalStateException("챕터 영상 경로가 없습니다. chapterId=" + chapterId);
            }

            Path videoPath = Path.of(chapter.getVideoPath());
            audioPath = ffmpegService.convertVideoToMp3(videoPath);

            String transcriptText = sttService.transcribe(audioPath);
            courseChapterMapper.updateTranscriptText(chapterId, transcriptText);

            quizGenerateService.generateAndSaveQuizzes(
                    chapterId,
                    DEFAULT_QUIZ_COUNT,
                    DEFAULT_DIFFICULTY
            );

            courseChapterMapper.updateQuizBuildStatus(chapterId, QuizBuildStatus.COMPLETED);
        } catch (Exception e) {
            log.error("퀴즈 빌드 실패. chapterId={}", chapterId, e);
            courseChapterMapper.updateQuizBuildStatus(chapterId, QuizBuildStatus.FAILED);
        } finally {
            ffmpegService.deleteIfExists(audioPath);
        }
    }
}