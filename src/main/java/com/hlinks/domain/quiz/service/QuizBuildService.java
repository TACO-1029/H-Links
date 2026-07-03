package com.hlinks.domain.quiz.service;

import com.hlinks.domain.course.entity.CourseChapter;
import com.hlinks.domain.course.mapper.CourseChapterMapper;
import com.hlinks.domain.quiz.ffmpeg.FfmpegService;
import com.hlinks.domain.quiz.stt.SttService;
import com.hlinks.domain.quiz.type.QuizBuildStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizBuildService {

    private static final int DEFAULT_QUIZ_COUNT = 3;
    private static final String DEFAULT_DIFFICULTY = "MEDIUM";

    private final CourseChapterMapper courseChapterMapper;
    private final FfmpegService ffmpegService;
    private final SttService sttService;
    private final QuizGenerateService quizGenerateService;

    @Value("${file.upload.root:./storage/uploads}")
    private String uploadRoot;

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

            Path videoPath = resolveVideoPath(chapter.getVideoPath());
            validateResolvedVideoPath(chapterId, videoPath);
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
            deleteAudioFile(audioPath);
        }
    }

    private Path resolveVideoPath(String videoPath) {
        Path rawVideoPath = Path.of(videoPath.trim());

        if (rawVideoPath.isAbsolute()) {
            return rawVideoPath.toAbsolutePath().normalize();
        }

        return Path.of(uploadRoot)
                .toAbsolutePath()
                .normalize()
                .resolve(rawVideoPath)
                .normalize();
    }

    private void validateResolvedVideoPath(Long chapterId, Path videoPath) {
        if (videoPath == null || !Files.exists(videoPath) || !Files.isRegularFile(videoPath) || !Files.isReadable(videoPath)) {
            throw new IllegalStateException(
                    "챕터 영상 파일을 찾을 수 없거나 읽을 수 없습니다. chapterId=" + chapterId + ", path=" + videoPath
            );
        }
    }

    private void deleteAudioFile(Path audioPath) {
        if (audioPath == null) {
            return;
        }

        try {
            ffmpegService.deleteIfExists(audioPath);
        } catch (Exception e) {
            log.warn("퀴즈 빌드 임시 오디오 파일 삭제 실패. path={}", audioPath, e);
        }
    }
}
