package com.hlinks.domain.coffeechat.mapper;

import com.hlinks.domain.coffeechat.dto.CoffeeChatHistoryDto;
import com.hlinks.domain.coffeechat.dto.CoffeeChatUserProfileDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CoffeeChatMapper {

    String findEmailNotifyYnByUserId(@Param("userId") Long userId);

    void upsertCoffeeChatSetting(
            @Param("userId") Long userId,
            @Param("emailNotifyYn") String emailNotifyYn
    );

    CoffeeChatUserProfileDto findUserProfileForCoffeeChat(@Param("userId") Long userId);

    int countRequestedCoffeeChat(
            @Param("requesterUserId") Long requesterUserId,
            @Param("receiverUserId") Long receiverUserId
    );

    void insertCoffeeChatRequest(
            @Param("requestId") Long requestId,
            @Param("requesterUserId") Long requesterUserId,
            @Param("receiverUserId") Long receiverUserId,
            @Param("requesterKcyCode") String requesterKcyCode,
            @Param("receiverKcyCode") String receiverKcyCode,
            @Param("matchGrade") String matchGrade,
            @Param("message") String message,
            @Param("status") String status
    );

    Long nextCoffeeChatRequestId();

    Long nextMailLogId();

    void insertMailSendLog(
            @Param("mailLogId") Long mailLogId,
            @Param("requestId") Long requestId,
            @Param("receiverUserId") Long receiverUserId,
            @Param("toEmail") String toEmail,
            @Param("subject") String subject,
            @Param("content") String content,
            @Param("sendStatus") String sendStatus,
            @Param("errorMessage") String errorMessage
    );

    void updateMailSendLogStatus(
            @Param("mailLogId") Long mailLogId,
            @Param("sendStatus") String sendStatus,
            @Param("errorMessage") String errorMessage
    );

    List<CoffeeChatHistoryDto> findSentRequests(@Param("userId") Long userId);

    List<CoffeeChatHistoryDto> findReceivedRequests(@Param("userId") Long userId);

    int updateReceivedRequestStatus(
            @Param("receiverUserId") Long receiverUserId,
            @Param("requestId") Long requestId,
            @Param("status") String status
    );

    int deleteReceivedRequest(
            @Param("receiverUserId") Long receiverUserId,
            @Param("requestId") Long requestId
    );

    void deleteMailLogsByReceivedRequest(
            @Param("receiverUserId") Long receiverUserId,
            @Param("requestId") Long requestId
    );
}
