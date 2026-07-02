package com.hlinks.domain.course.service;

import com.hlinks.domain.course.dto.ChapterTranscriptResponse;
import com.hlinks.domain.course.entity.CourseChapter;
import com.hlinks.domain.course.mapper.CourseChapterMapper;
import com.hlinks.domain.quiz.ffmpeg.FfmpegService;
import com.hlinks.domain.quiz.stt.SttService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class CourseChapterTranscriptService {

    private final FfmpegService ffmpegService;
    private final SttService sttService;
    private final CourseChapterMapper courseChapterMapper;

    public ChapterTranscriptResponse generateAndSaveTranscript(Long chapterId, Path videoPath) {
        validateVideoPath(videoPath);

        CourseChapter chapter = courseChapterMapper.findById(chapterId);
        if (chapter == null) {
            throw new IllegalArgumentException("존재하지 않는 챕터입니다. chapterId=" + chapterId);
        }

        Path mp3Path = null;

        try {
            mp3Path = ffmpegService.convertVideoToMp3(videoPath);

            String transcriptText = sttService.transcribe(mp3Path);

            if (transcriptText == null || transcriptText.isBlank()) {
                throw new IllegalStateException("STT 결과가 비어 있습니다.");
            }

            int updatedCount = courseChapterMapper.updateTranscriptText(chapterId, transcriptText);

            if (updatedCount != 1) {
                throw new IllegalStateException("STT 결과 저장에 실패했습니다. chapterId=" + chapterId);
            }

            ChapterTranscriptResponse response = new ChapterTranscriptResponse();
            response.setChapterId(chapterId);
            response.setTranscriptText(transcriptText);
            return response;

        } finally {
            ffmpegService.deleteIfExists(mp3Path);
        }
    }

    private void validateVideoPath(Path videoPath) {
        if (videoPath == null) {
            throw new IllegalArgumentException("videoPath는 필수입니다.");
        }

        Path normalizedPath = videoPath.toAbsolutePath().normalize();

        if (!Files.exists(normalizedPath)) {
            throw new IllegalArgumentException("영상 파일이 존재하지 않습니다. path=" + normalizedPath);
        }

        if (!Files.isRegularFile(normalizedPath)) {
            throw new IllegalArgumentException("영상 경로가 일반 파일이 아닙니다. path=" + normalizedPath);
        }

        if (!Files.isReadable(normalizedPath)) {
            throw new IllegalArgumentException("영상 파일을 읽을 수 없습니다. path=" + normalizedPath);
        }
    }
}