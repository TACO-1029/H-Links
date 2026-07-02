package com.hlinks.domain.quiz.ffmpeg;

import com.hlinks.global.response.code.ErrorResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FfmpegService {

    private final FfmpegProperties properties;

    /**
     * 서버 환경에서 ffmpeg 실행 가능 여부 확인
     */
    public boolean isFfmpegAvailable() {
        ProcessBuilder processBuilder = new ProcessBuilder(
                properties.getCommand(),
                "-version"
        );
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                return false;
            }

            return process.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    /**
     * 영상 파일을 mp3 파일로 변환한다.
     *
     * 반환된 mp3 파일은 호출자가 STT 처리 후 삭제하는 것을 원칙으로 한다.
     */
    public Path convertVideoToMp3(Path videoPath) {
        validateInputFile(videoPath);

        Path tempDir = createTempDirectory();
        Path outputPath = tempDir.resolve(createOutputFileName());

        List<String> command = List.of(
                properties.getCommand(),
                "-y",
                "-i", videoPath.toAbsolutePath().normalize().toString(),
                "-vn",
                "-acodec", "libmp3lame",
                "-ar", "16000",
                "-ac", "1",
                outputPath.toAbsolutePath().normalize().toString()
        );

        executeFfmpeg(command, outputPath);

        return outputPath;
    }

    private void validateInputFile(Path videoPath) {
        if (videoPath == null) {
            throw new FfmpegException(ErrorResponseCode.FFMPEG_INVALID_INPUT);
        }

        Path normalizedPath = videoPath.toAbsolutePath().normalize();

        if (!Files.exists(normalizedPath)) {
            throw new FfmpegException(ErrorResponseCode.FFMPEG_INVALID_INPUT);
        }

        if (!Files.isRegularFile(normalizedPath)) {
            throw new FfmpegException(ErrorResponseCode.FFMPEG_INVALID_INPUT);
        }

        if (!Files.isReadable(normalizedPath)) {
            throw new FfmpegException(ErrorResponseCode.FFMPEG_INVALID_INPUT);
        }
    }

    private Path createTempDirectory() {
        try {
            Path tempDir = Path.of(properties.getTempDir())
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(tempDir);

            return tempDir;
        } catch (IOException e) {
            throw new FfmpegException(ErrorResponseCode.FFMPEG_TEMP_DIRECTORY_CREATE_FAILED, e);
        }
    }

    private String createOutputFileName() {
        return "audio-" + UUID.randomUUID() + ".mp3";
    }

    private void executeFfmpeg(List<String> command, Path outputPath) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();

            // waitFor와 동시에 출력 스트림을 소비하여 파이프 버퍼 포화로 인한 데드락을 방지
            StringBuilder logs = new StringBuilder();
            Thread logReader = new Thread(() -> {
                try (var reader = process.inputReader(StandardCharsets.UTF_8)) {
                    reader.lines().forEach(line -> logs.append(line).append(System.lineSeparator()));
                } catch (IOException ignored) {
                }
            });
            logReader.start();

            boolean finished = process.waitFor(
                    properties.getTimeoutSeconds(),
                    TimeUnit.SECONDS
            );

            logReader.join(TimeUnit.SECONDS.toMillis(5));

            if (!finished) {
                process.destroyForcibly();
                deleteIfExists(outputPath);
                throw new FfmpegException(ErrorResponseCode.FFMPEG_CONVERT_TIMEOUT);
            }

            int exitCode = process.exitValue();

            if (exitCode != 0) {
                deleteIfExists(outputPath);
                throw new FfmpegException(ErrorResponseCode.FFMPEG_CONVERT_FAILED);
            }

            if (!Files.exists(outputPath) || Files.size(outputPath) == 0) {
                deleteIfExists(outputPath);
                throw new FfmpegException(ErrorResponseCode.FFMPEG_OUTPUT_NOT_FOUND);
            }

        } catch (IOException e) {
            deleteIfExists(outputPath);
            throw new FfmpegException(ErrorResponseCode.FFMPEG_IO_ERROR, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            deleteIfExists(outputPath);
            throw new FfmpegException(ErrorResponseCode.FFMPEG_INTERRUPTED, e);
        }
    }

    public void deleteIfExists(Path path) {
        if (path == null) {
            return;
        }

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new FfmpegException(ErrorResponseCode.FFMPEG_TEMP_FILE_DELETE_FAILED, e);
        }
    }
}
