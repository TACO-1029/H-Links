package com.hlinks.domain.course.service;

import com.hlinks.domain.course.dto.AdminCourseCreateRequest;
import com.hlinks.domain.course.dto.AdminCourseCreateResponse;
import com.hlinks.domain.course.entity.Course;
import com.hlinks.domain.course.entity.CourseChapter;
import com.hlinks.domain.course.mapper.CourseMapper;
import com.hlinks.domain.course.type.CourseType;
import com.hlinks.domain.quiz.ffmpeg.FfmpegService;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;
import com.hlinks.global.storage.DownloadedFile;
import com.hlinks.global.storage.FileStorageService;
import com.hlinks.global.storage.StoredFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.web.util.UriUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminCourseService {

    private static final String FIXED_VIDEO_FILE_NAME = "lecture.mp4";
    private static final String CAREER_PATH_CATEGORY = "CAREER_PATH";
    private static final String THUMBNAIL_FILE_PREFIX = "thumbnail.";
    private static final Set<String> ALLOWED_THUMBNAIL_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_COURSE_MATERIAL_EXTENSIONS =
            Set.of("pdf", "ppt", "pptx", "doc", "docx", "xls", "xlsx", "zip");

    private final CourseMapper courseMapper;
    private final FfmpegService ffmpegService;
    private final FileStorageService fileStorageService;

    @Transactional
    public AdminCourseCreateResponse createCourse(AdminCourseCreateRequest request, Long createdBy) {
        validateRequest(request);
        validateCreatedBy(createdBy);

        Course course = toCourse(request, createdBy);
        int courseInserted = courseMapper.insertCourse(course);

        if (courseInserted != 1 || course.getCourseId() == null) {
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "강의 저장에 실패했습니다.");
        }

        List<String> savedFileKeys = new ArrayList<>();

        try {
            StoredFile savedThumbnail = saveThumbnailFile(course.getCourseId(), request.getThumbnailFile());
            savedFileKeys.add(savedThumbnail.key());

            String thumbnailUrl = buildThumbnailUrl(course.getCourseId(), getThumbnailExtension(request.getThumbnailFile()));
            updateThumbnailMetadata(course.getCourseId(), thumbnailUrl);

            if (isOffline(request)) {
                int offlineCourseInserted = courseMapper.insertOfflineCourse(
                        course.getCourseId(),
                        request.getCapacity(),
                        request.getLocation().trim(),
                        request.getApplyStartDate(),
                        request.getApplyEndDate(),
                        request.getCourseStartDate(),
                        request.getCourseEndDate()
                );

                if (offlineCourseInserted != 1) {
                    throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "오프라인 강의 정보 저장에 실패했습니다.");
                }

                List<Long> chapterIds = insertCourseChapters(course.getCourseId(), request.getChapterTitles());
                AdminCourseCreateResponse response = new AdminCourseCreateResponse();
                response.setCourseId(course.getCourseId());
                response.setChapterIds(chapterIds);
                return response;
            }

            String courseMaterialUrl = null;
            if (hasCourseMaterialFile(request.getCourseMaterialFile())) {
                StoredFile savedMaterial = saveCourseMaterialFile(course.getCourseId(), request.getCourseMaterialFile());
                savedFileKeys.add(savedMaterial.key());
                courseMaterialUrl = buildCourseMaterialUrl(course.getCourseId(), getCourseMaterialFileName(request.getCourseMaterialFile()));
            }

            int onlineCourseInserted = courseMapper.insertOnlineCourse(course.getCourseId(), courseMaterialUrl);

            if (onlineCourseInserted != 1) {
                throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "온라인 강의 정보 저장에 실패했습니다.");
            }

            List<Long> chapterIds = new ArrayList<>();
            for (int index = 0; index < request.getChapterTitles().size(); index++) {
                CourseChapter chapter = insertCourseChapter(course.getCourseId(), request.getChapterTitles().get(index), index + 1);
                MultipartFile videoFile = request.getVideoFiles().get(index);
                StoredFile savedVideo = saveVideoFile(course.getCourseId(), chapter.getChapterId(), videoFile);
                savedFileKeys.add(savedVideo.key());

                Integer durationSeconds = getVideoDurationSeconds(savedVideo.key());
                updateVideoMetadata(course.getCourseId(), chapter.getChapterId(), videoFile, durationSeconds);
                chapterIds.add(chapter.getChapterId());
            }

            AdminCourseCreateResponse response = new AdminCourseCreateResponse();
            response.setCourseId(course.getCourseId());
            response.setChapterIds(chapterIds);
            return response;
        } catch (BaseException e) {
            deleteSavedFiles(savedFileKeys);
            throw e;
        } catch (Exception e) {
            deleteSavedFiles(savedFileKeys);
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "강의 파일 저장에 실패했습니다.", e);
        }
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

        if (request.getCourseType() == null) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "강의 유형은 필수입니다.");
        }

        validateCategoryCourseType(request);

        if (!StringUtils.hasText(request.getInstructorName())) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "강사명은 필수입니다.");
        }

        validateThumbnailFile(request.getThumbnailFile());
        validateChapterTitles(request);

        if (CourseType.ONLINE.equals(request.getCourseType())) {
            validateCourseMaterialFile(request.getCourseMaterialFile());
            validateOnlineChapterVideos(request);
            return;
        }

        validateOfflineCourse(request);
    }

    private void validateCategoryCourseType(AdminCourseCreateRequest request) {
        if (CAREER_PATH_CATEGORY.equals(request.getCategoryType().trim())
                && CourseType.OFFLINE.equals(request.getCourseType())) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "커리어패스는 온라인 강의만 등록할 수 있습니다.");
        }
    }

    private void validateCreatedBy(Long createdBy) {
        if (createdBy == null) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "등록 관리자 정보가 필요합니다.");
        }
    }

    private void validateChapterTitles(AdminCourseCreateRequest request) {
        List<String> chapterTitles = request.getChapterTitles();

        if (chapterTitles == null || chapterTitles.isEmpty()) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "챕터명은 1개 이상 필요합니다.");
        }

        for (String chapterTitle : chapterTitles) {
            if (!StringUtils.hasText(chapterTitle)) {
                throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "챕터명은 필수입니다.");
            }
        }
    }

    private void validateOnlineChapterVideos(AdminCourseCreateRequest request) {
        List<String> chapterTitles = request.getChapterTitles();
        List<MultipartFile> videoFiles = request.getVideoFiles();

        if (videoFiles == null || videoFiles.isEmpty()) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "mp4 영상 파일은 1개 이상 필요합니다.");
        }

        if (chapterTitles.size() != videoFiles.size()) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "챕터명과 영상 파일 개수가 일치해야 합니다.");
        }

        for (MultipartFile videoFile : videoFiles) {
            validateVideoFile(videoFile);
        }
    }

    private void validateOfflineCourse(AdminCourseCreateRequest request) {
        if (request.getCapacity() == null || request.getCapacity() < 1) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "오프라인 강의 정원은 필수입니다.");
        }

        if (!StringUtils.hasText(request.getLocation())) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "오프라인 강의 장소는 필수입니다.");
        }

        if (request.getApplyStartDate() == null || request.getApplyEndDate() == null) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "신청 기간은 필수입니다.");
        }

        if (request.getCourseStartDate() == null || request.getCourseEndDate() == null) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "강의 기간은 필수입니다.");
        }
    }

    private void validateThumbnailFile(MultipartFile thumbnailFile) {
        if (thumbnailFile == null || thumbnailFile.isEmpty()) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "강의 썸네일 이미지는 필수입니다.");
        }

        String extension = getThumbnailExtension(thumbnailFile);

        if (!ALLOWED_THUMBNAIL_EXTENSIONS.contains(extension)) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "썸네일은 jpg, jpeg, png, webp 파일만 등록할 수 있습니다.");
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

    private void validateCourseMaterialFile(MultipartFile courseMaterialFile) {
        if (!hasCourseMaterialFile(courseMaterialFile)) {
            return;
        }

        String extension = getLowercaseExtension(courseMaterialFile.getOriginalFilename());

        if (!ALLOWED_COURSE_MATERIAL_EXTENSIONS.contains(extension)) {
            throw new BaseException(
                    ErrorResponseCode.INVALID_REQUEST_PARAMETER,
                    "강의 자료는 pdf, ppt, pptx, doc, docx, xls, xlsx, zip 파일만 등록할 수 있습니다."
            );
        }
    }

    private Course toCourse(AdminCourseCreateRequest request, Long createdBy) {
        Course course = new Course();
        course.setCreatedBy(createdBy);
        course.setCategoryType(request.getCategoryType().trim());
        course.setCourseType(request.getCourseType());
        course.setCourseTitle(request.getCourseTitle().trim());
        course.setDescription(request.getDescription());
        course.setInstructorName(request.getInstructorName().trim());
        return course;
    }

    private boolean isOffline(AdminCourseCreateRequest request) {
        return CourseType.OFFLINE.equals(request.getCourseType());
    }

    private CourseChapter toInitialChapter(Long courseId, String chapterTitle, int chapterOrder) {
        CourseChapter chapter = new CourseChapter();
        chapter.setCourseId(courseId);
        chapter.setChapterTitle(chapterTitle.trim());
        chapter.setChapterOrder(chapterOrder);
        return chapter;
    }

    private List<Long> insertCourseChapters(Long courseId, List<String> chapterTitles) {
        List<Long> chapterIds = new ArrayList<>();

        for (int index = 0; index < chapterTitles.size(); index++) {
            CourseChapter chapter = insertCourseChapter(courseId, chapterTitles.get(index), index + 1);
            chapterIds.add(chapter.getChapterId());
        }

        return chapterIds;
    }

    private CourseChapter insertCourseChapter(Long courseId, String chapterTitle, int chapterOrder) {
        CourseChapter chapter = toInitialChapter(courseId, chapterTitle, chapterOrder);
        int chapterInserted = courseMapper.insertCourseChapter(chapter);

        if (chapterInserted != 1 || chapter.getChapterId() == null) {
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "강의 챕터 저장에 실패했습니다.");
        }

        return chapter;
    }

    private StoredFile saveThumbnailFile(Long courseId, MultipartFile thumbnailFile) {
        return fileStorageService.upload(toThumbnailKey(courseId, getThumbnailFileName(thumbnailFile)), thumbnailFile);
    }

    private void updateThumbnailMetadata(Long courseId, String thumbnailUrl) {
        int updatedCount = courseMapper.updateCourseThumbnail(courseId, thumbnailUrl);

        if (updatedCount != 1) {
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "강의 썸네일 정보 저장에 실패했습니다.");
        }
    }

    private StoredFile saveVideoFile(Long courseId, Long chapterId, MultipartFile videoFile) {
        return fileStorageService.upload(toRelativeVideoPath(courseId, chapterId), videoFile);
    }

    private StoredFile saveCourseMaterialFile(Long courseId, MultipartFile courseMaterialFile) {
        return fileStorageService.upload(toCourseMaterialKey(courseId, getCourseMaterialFileName(courseMaterialFile)), courseMaterialFile);
    }

    private Integer getVideoDurationSeconds(String videoKey) {
        try (DownloadedFile downloadedFile = fileStorageService.download(videoKey)) {
            return ffmpegService.getVideoDurationSeconds(downloadedFile.path());
        }
    }

    private void updateVideoMetadata(
            Long courseId,
            Long chapterId,
            MultipartFile videoFile,
            Integer durationSeconds
    ) {
        CourseChapter chapter = new CourseChapter();
        chapter.setCourseId(courseId);
        chapter.setChapterId(chapterId);
        chapter.setVideoUrl(buildVideoUrl(courseId, chapterId));
        chapter.setVideoPath(toRelativeVideoPath(courseId, chapterId));
        chapter.setOriginalFileName(videoFile.getOriginalFilename());
        chapter.setFileSize(videoFile.getSize());
        chapter.setDurationSeconds(durationSeconds);

        int updatedCount = courseMapper.updateCourseChapterVideo(chapter);

        if (updatedCount != 1) {
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "영상 파일 정보 저장에 실패했습니다.");
        }
    }

    public String resolveVideoKey(Long courseId, Long chapterId) {
        return toRelativeVideoPath(courseId, chapterId);
    }

    public String resolveThumbnailKey(Long courseId, String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.startsWith(THUMBNAIL_FILE_PREFIX)) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "강의 썸네일 경로가 올바르지 않습니다.");
        }

        return toThumbnailKey(courseId, fileName);
    }

    public String resolveCourseMaterialKey(Long courseId, String fileName) {
        if (!StringUtils.hasText(fileName)) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "강의 자료 경로가 올바르지 않습니다.");
        }

        String safeFileName = getSafeRequestedFileName(fileName);
        if (!fileName.equals(safeFileName)) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "강의 자료 경로가 올바르지 않습니다.");
        }

        return toCourseMaterialKey(courseId, fileName);
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

    private String buildThumbnailUrl(Long courseId, String extension) {
        return "/images/courses/" + courseId + "/" + THUMBNAIL_FILE_PREFIX + extension;
    }

    private String buildCourseMaterialUrl(Long courseId, String fileName) {
        return "/materials/courses/" + courseId + "/" + UriUtils.encodePathSegment(fileName, StandardCharsets.UTF_8);
    }

    private String toThumbnailKey(Long courseId, String fileName) {
        return Path.of("courses", String.valueOf(courseId), fileName).toString();
    }

    private String toCourseMaterialKey(Long courseId, String fileName) {
        return Path.of("courses", String.valueOf(courseId), "materials", fileName).toString();
    }

    private String getThumbnailFileName(MultipartFile thumbnailFile) {
        return THUMBNAIL_FILE_PREFIX + getThumbnailExtension(thumbnailFile);
    }

    private String getThumbnailExtension(MultipartFile thumbnailFile) {
        return getLowercaseExtension(thumbnailFile.getOriginalFilename());
    }

    private String getLowercaseExtension(String originalFileName) {
        String extension = StringUtils.getFilenameExtension(originalFileName);
        if (extension == null) {
            return "";
        }

        return extension.toLowerCase(Locale.ROOT);
    }

    private String getCourseMaterialFileName(MultipartFile courseMaterialFile) {
        String originalFileName = courseMaterialFile.getOriginalFilename();
        String cleanedFileName = StringUtils.cleanPath(originalFileName == null ? "" : originalFileName)
                .replace('\\', '/');

        int lastSlashIndex = cleanedFileName.lastIndexOf('/');
        if (lastSlashIndex >= 0) {
            cleanedFileName = cleanedFileName.substring(lastSlashIndex + 1);
        }

        String safeFileName = cleanedFileName.replaceAll("[\\\\/:*?\"<>|]+", "_").trim();
        if (!StringUtils.hasText(safeFileName) || ".".equals(safeFileName) || "..".equals(safeFileName)) {
            return "material." + getLowercaseExtension(originalFileName);
        }

        return safeFileName;
    }

    private boolean hasCourseMaterialFile(MultipartFile courseMaterialFile) {
        return courseMaterialFile != null && !courseMaterialFile.isEmpty();
    }

    private String getSafeRequestedFileName(String fileName) {
        String cleanedFileName = StringUtils.cleanPath(fileName == null ? "" : fileName).replace('\\', '/');
        int lastSlashIndex = cleanedFileName.lastIndexOf('/');
        return lastSlashIndex >= 0 ? cleanedFileName.substring(lastSlashIndex + 1) : cleanedFileName;
    }

    private void deleteSavedFiles(List<String> savedFileKeys) {
        for (String savedFileKey : savedFileKeys) {
            deleteSavedFile(savedFileKey);
        }
    }

    private void deleteSavedFile(String savedFileKey) {
        if (!StringUtils.hasText(savedFileKey)) {
            return;
        }

        try {
            fileStorageService.delete(savedFileKey);
        } catch (Exception e) {
            log.warn("강의 생성 실패 후 저장 파일 정리 실패. key={}", savedFileKey, e);
        }
    }
}
