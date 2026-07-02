package com.hlinks.domain.quiz.dto;

import com.hlinks.domain.quiz.type.QuestionType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/* 관리자가 직접 퀴즈를 만들거나, AI 생성 결과를 저장할 때 사용 */
@Getter
@Setter
public class QuizCreateRequest {

    private Long courseId;
    private Long chapterId;

    private QuestionType questionType;

    private String questionText;
    private String explanation;
    private String difficulty;
    private String answerText;
    private String status;
    private String aiGeneratedYn;
    private List<QuizOptionCreateRequest> options;
}
