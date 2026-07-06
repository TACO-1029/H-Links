package com.hlinks.domain.recommend.course.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CourseRecommendationRequest {

    @NotBlank(message = "핵심 기술 분야는 필수입니다.")
    private String category;

    @Valid
    @NotEmpty(message = "레벨테스트 결과는 최소 1개 이상 필요합니다.")
    @Size(max = 3, message = "레벨테스트 결과는 최대 3개까지 처리할 수 있습니다.")
    private List<LevelTestSkillResultRequest> results;

    private Integer limit;
}
