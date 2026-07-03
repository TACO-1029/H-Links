package com.hlinks.domain.course.service;

import com.hlinks.domain.course.dto.AdminCourseCreateRequest;
import com.hlinks.domain.course.dto.AdminCourseCreateResponse;
import com.hlinks.domain.course.entity.Course;
import com.hlinks.domain.course.entity.CourseChapter;
import com.hlinks.domain.course.mapper.CourseMapper;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminCourseService {

    private static final String ONLINE_COURSE_TYPE = "ONLINE";
    private static final String FIXED_VIDEO_FILE_NAME = "lecture.mp4";

    private final CourseMapper courseMapper;

    @Value("${file.upload.root:./storage/uploads}")
    private String uploadRoot;

    @Transactional
    public AdminCourseCreateResponse createCourse(AdminCourseCreateRequest request, Long createdBy) {
        validateRequest(request);
        validateCreatedBy(createdBy);

        Course course = toCourse(request, createdBy);
        int courseInserted = courseMapper.insertCourse(course);

        if (courseInserted != 1 || course.getCourseId() == null) {
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "강의 저장에 실패했습니다.");
        }

        int onlineCourseInserted = courseMapper.insertOnlineCourse(course.getCourseId());

        if (onlineCourseInserted != 1) {
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "온라인 강의 정보 저장에 실패했습니다.");
        }

        List<Path> savedVideoPaths = new ArrayList<>();
        List<Long> chapterIds = new ArrayList<>();

        try {
            for (int index = 0; index < request.getVideoFiles().size(); index++) {
                CourseChapter chapter = toInitialChapter(course.getCourseId(), request.getChapterTitles().get(index), index + 1);
                int chapterInserted = courseMapper.insertCourseChapter(chapter);

                if (chapterInserted != 1 || chapter.getChapterId() == null) {
                    throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "강의 챕터 저장에 실패했습니다.");
                }

                MultipartFile videoFile = request.getVideoFiles().get(index);
                Path savedVideoPath = saveVideoFile(course.getCourseId(), chapter.getChapterId(), videoFile);
                savedVideoPaths.add(savedVideoPath);

                updateVideoMetadata(course.getCourseId(), chapter.getChapterId(), videoFile);
                chapterIds.add(chapter.getChapterId());
            }
        } catch (BaseException e) {
            deleteSavedFiles(savedVideoPaths);
            throw e;
        } catch (Exception e) {
            deleteSavedFiles(savedVideoPaths);
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "영상 파일 저장에 실패했습니다.", e);
        }

        AdminCourseCreateResponse response = new AdminCourseCreateResponse();
        response.setCourseId(course.getCourseId());
        response.setChapterIds(chapterIds);
        return response;
    }

    private void validateRequest(AdminCourseCreateRequest request) {
        if (request == null) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_BODY);
        }

        if (!StringUtils.hasText(request.getCourseTitle())) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "강의명은 필수입니다.");
        }

        if (!StringUtils.hasText(request.getCategoryType())) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "카테고리는 필수입니다.");
        }

        if (!StringUtils.hasText(request.getInstructorName())) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "강사명은 필수입니다.");
        }

        validateChapters(request);
    }

    private void validateCreatedBy(Long createdBy) {
        if (createdBy == null) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "등록 관리자 정보가 필요합니다.");
        }
    }

    private void validateChapters(AdminCourseCreateRequest request) {
        List<String> chapterTitles = request.getChapterTitles();
        List<MultipartFile> videoFiles = request.getVideoFiles();

        if (chapterTitles == null || chapterTitles.isEmpty()) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "챕터명은 1개 이상 필요합니다.");
        }

        if (videoFiles == null || videoFiles.isEmpty()) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "mp4 영상 파일은 1개 이상 필요합니다.");
        }

        if (chapterTitles.size() != videoFiles.size()) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "챕터명과 영상 파일 개수가 일치해야 합니다.");
        }

        for (int index = 0; index < videoFiles.size(); index++) {
            if (!StringUtils.hasText(chapterTitles.get(index))) {
                throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "챕터명은 필수입니다.");
            }

            validateVideoFile(videoFiles.get(index));
        }
    }

    private void validateVideoFile(MultipartFile videoFile) {
        if (videoFile == null || videoFile.isEmpty()) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "mp4 영상 파일은 필수입니다.");
        }

        String originalFileName = videoFile.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFileName);

        if (extension == null || !"mp4".equals(extension.toLowerCase(Locale.ROOT))) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "mp4 파일만 등록할 수 있습니다.");
        }
    }

    private Course toCourse(AdminCourseCreateRequest request, Long createdBy) {
        Course course = new Course();
        course.setCreatedBy(createdBy);
        course.setCategoryType(request.getCategoryType().trim());
        course.setCourseType(ONLINE_COURSE_TYPE);
        course.setCourseTitle(request.getCourseTitle().trim());
        course.setDescription(request.getDescription());
        course.setInstructorName(request.getInstructorName().trim());
        return course;
    }

    private CourseChapter toInitialChapter(Long courseId, String chapterTitle, int chapterOrder) {
        CourseChapter chapter = new CourseChapter();
        chapter.setCourseId(courseId);
        chapter.setChapterTitle(chapterTitle.trim());
        chapter.setChapterOrder(chapterOrder);
        return chapter;
    }

    private Path saveVideoFile(Long courseId, Long chapterId, MultipartFile videoFile) {
        Path targetPath = resolveVideoPath(courseId, chapterId);

        try {
            Files.createDirectories(targetPath.getParent());

            try (InputStream inputStream = videoFile.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return targetPath;
        } catch (IOException e) {
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "영상 파일 저장에 실패했습니다.", e);
        }
    }

    private void updateVideoMetadata(Long courseId, Long chapterId, MultipartFile videoFile) {
        CourseChapter chapter = new CourseChapter();
        chapter.setCourseId(courseId);
        chapter.setChapterId(chapterId);
        chapter.setVideoUrl(buildVideoUrl(courseId, chapterId));
        chapter.setVideoPath(toRelativeVideoPath(courseId, chapterId));
        chapter.setOriginalFileName(videoFile.getOriginalFilename());
        chapter.setFileSize(videoFile.getSize());

        int updatedCount = courseMapper.updateCourseChapterVideo(chapter);

        if (updatedCount != 1) {
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "영상 파일 정보 저장에 실패했습니다.");
        }
    }

    public Path resolveVideoPath(Long courseId, Long chapterId) {
        return Path.of(uploadRoot)
                .toAbsolutePath()
                .normalize()
                .resolve(toRelativeVideoPath(courseId, chapterId))
                .normalize();
    }

    private String toRelativeVideoPath(Long courseId, Long chapterId) {
        return Path.of(
                "courses",
                String.valueOf(courseId),
                "chapters",
                String.valueOf(chapterId),
                FIXED_VIDEO_FILE_NAME
        ).toString();
    }

    private String buildVideoUrl(Long courseId, Long chapterId) {
        return "/videos/courses/" + courseId + "/chapters/" + chapterId;
    }

    private void deleteSavedFiles(List<Path> savedVideoPaths) {
        for (Path savedVideoPath : savedVideoPaths) {
            deleteSavedFile(savedVideoPath);
        }
    }

    private void deleteSavedFile(Path savedVideoPath) {
        if (savedVideoPath == null) {
            return;
        }

        try {
            Files.deleteIfExists(savedVideoPath);
        } catch (IOException ignored) {
        }
    }
}
