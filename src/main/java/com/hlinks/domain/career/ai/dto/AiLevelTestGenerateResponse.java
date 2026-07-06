package com.hlinks.domain.career.ai.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class AiLevelTestGenerateResponse {
    private List<AiGeneratedLevelTestQuestion> questions;
}
