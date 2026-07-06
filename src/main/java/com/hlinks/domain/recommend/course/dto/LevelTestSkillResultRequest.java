package com.hlinks.domain.recommend.course.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LevelTestSkillResultRequest {

    @NotNull(message = "skillId는 필수입니다.")
    private Long skillId;

    @NotNull(message = "점수는 필수입니다.")
    @Min(value = 0, message = "점수는 0점 이상이어야 합니다.")
    @Max(value = 100, message = "점수는 100점 이하여야 합니다.")
    private Integer score;

    @NotBlank(message = "선택 난이도는 필수입니다.")
    private String selectedDifficulty;
}
