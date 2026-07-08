package com.hlinks.domain.coffeechat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoffeeChatHistoryDto {

    private Long requestId;
    private Long partnerUserId;
    private String partnerName;
    private String partnerDepartmentName;
    private String partnerPositionName;
    private String requesterKcyCode;
    private String receiverKcyCode;
    private String matchGrade;
    private String message;
    private String status;
    private String createdAt;

    public String getStatusLabel() {
        if (status == null) {
            return "상태 미정";
        }

        return switch (status) {
            case "REQUESTED" -> "대기 중";
            case "MAILED" -> "메일 발송됨";
            case "ACCEPTED" -> "승인됨";
            case "REJECTED" -> "거절됨";
            case "CANCELED" -> "취소됨";
            default -> "상태 미정";
        };
    }
}
