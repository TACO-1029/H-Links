package com.hlinks.domain.course.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChapterTranscriptResponse {

    private Long chapterId;
    private String transcriptText;
}
