package com.hlinks.domain.course.service;

import com.hlinks.domain.course.ai.dto.CourseSummarySkill;
import com.hlinks.domain.course.ai.dto.CourseSummaryGenerateResponse;
import com.hlinks.domain.course.ai.service.AiCourseSummaryService;
import com.hlinks.domain.course.entity.CourseChapter;
import com.hlinks.domain.course.mapper.CourseChapterMapper;
import com.hlinks.domain.course.mapper.CourseChapterSkillMapper;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseChapterSummaryService {

    private final CourseChapterMapper courseChapterMapper;
    private final CourseChapterSkillMapper courseChapterSkillMapper;
    private final AiCourseSummaryService aiCourseSummaryService;

    @Transactional
    public CourseSummaryGenerateResponse generateAndSaveSummary(Long chapterId) {
        CourseSummaryGenerateResponse response = generateSummary(chapterId);
        saveSummaryText(chapterId, response.getSummaryText());
        saveChapterSkills(chapterId, response.getSkills());

        return response;
    }

    public CourseSummaryGenerateResponse generateSummary(Long chapterId) {
        CourseChapter chapter = findChapter(chapterId);

        if (!StringUtils.hasText(chapter.getTranscriptText())) {
            throw new BaseException(
                    ErrorResponseCode.BAD_REQUEST,
                    "챕터 transcript가 존재하지 않습니다. chapterId=" + chapterId
            );
        }

        return aiCourseSummaryService.generateSummary(chapter.getTranscriptText());
    }

    public void saveSummaryText(Long chapterId, String summaryText) {
        int updatedCount = courseChapterMapper.updateSummaryText(chapterId, summaryText);

        if (updatedCount == 0) {
            throw new BaseException(
                    ErrorResponseCode.NOT_FOUND_ENDPOINT,
                    "요약을 저장할 챕터를 찾을 수 없습니다. chapterId=" + chapterId
            );
        }
    }

    public void saveChapterSkills(Long chapterId, List<CourseSummarySkill> skills) {
        courseChapterSkillMapper.deleteByChapterId(chapterId);

        if (skills == null || skills.isEmpty()) {
            return;
        }

        Map<Long, Integer> matchedSkills = new LinkedHashMap<>();

        for (CourseSummarySkill skill : skills) {
            if (skill == null || !StringUtils.hasText(skill.getSkillName())) {
                continue;
            }

            String skillName = skill.getSkillName().trim();
            Long skillId = courseChapterSkillMapper.findSkillIdByName(skillName);

            if (skillId == null) {
                log.info("AI 강의 요약 skill 미매칭. chapterId={}, skillName={}", chapterId, skillName);
                continue;
            }

            matchedSkills.merge(skillId, normalizeWeight(skill.getWeight()), Math::max);
        }

        matchedSkills.forEach((skillId, weight) ->
                courseChapterSkillMapper.insertChapterSkill(chapterId, skillId, weight)
        );
    }

    private Integer normalizeWeight(Integer weight) {
        if (weight == null) {
            return 1;
        }

        return Math.max(1, Math.min(weight, 5));
    }

    private CourseChapter findChapter(Long chapterId) {
        if (chapterId == null) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "chapterId는 필수입니다.");
        }

        CourseChapter chapter = courseChapterMapper.findById(chapterId);

        if (chapter == null) {
            throw new BaseException(
                    ErrorResponseCode.NOT_FOUND_ENDPOINT,
                    "존재하지 않는 챕터입니다. chapterId=" + chapterId
            );
        }

        return chapter;
    }
}
