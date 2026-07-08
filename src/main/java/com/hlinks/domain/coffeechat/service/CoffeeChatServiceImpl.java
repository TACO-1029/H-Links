package com.hlinks.domain.coffeechat.service;

import com.hlinks.domain.competency.service.CompetencyScoreService;
import com.hlinks.domain.competency.type.CompetencyCalcType;
import com.hlinks.domain.coffeechat.dto.CoffeeChatCreateRequest;
import com.hlinks.domain.coffeechat.dto.CoffeeChatCreateResponse;
import com.hlinks.domain.coffeechat.dto.CoffeeChatHistoryDto;
import com.hlinks.domain.coffeechat.dto.CoffeeChatSettingResponse;
import com.hlinks.domain.coffeechat.dto.CoffeeChatSettingUpdateRequest;
import com.hlinks.domain.coffeechat.dto.CoffeeChatUserProfileDto;
import com.hlinks.domain.coffeechat.exception.CoffeeChatErrorCode;
import com.hlinks.domain.coffeechat.mapper.CoffeeChatMapper;
import com.hlinks.domain.coffeechat.type.CoffeeChatRequestStatus;
import com.hlinks.domain.coffeechat.type.MailSendStatus;
import com.hlinks.domain.recommend.kcy.type.KcyCompatibilityPolicy;
import com.hlinks.domain.recommend.kcy.type.KcyMatchGrade;
import com.hlinks.domain.recommend.kcy.type.KcyType;
import com.hlinks.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoffeeChatServiceImpl implements CoffeeChatService {

    private static final String REFERENCE_TYPE_KCY_COFFEE_CHAT_REQUEST = "KCY_COFFEE_CHAT_REQUEST";

    private final CoffeeChatMapper coffeeChatMapper;
    private final ObjectProvider<JavaMailSender> javaMailSenderProvider;
    private final CompetencyScoreService competencyScoreService;

    @Value("${hlinks.coffee-chat.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${hlinks.coffee-chat.mail.from:no-reply@hlinks.local}")
    private String mailFrom;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Override
    public CoffeeChatSettingResponse getSetting(Long userId) {
        return CoffeeChatSettingResponse.builder()
                .emailNotifyEnabled(isEmailNotifyEnabled(userId))
                .build();
    }

    @Override
    @Transactional
    public CoffeeChatSettingResponse updateSetting(Long userId, CoffeeChatSettingUpdateRequest request) {
        String emailNotifyYn = Boolean.TRUE.equals(request.getEmailNotifyEnabled()) ? "Y" : "N";

        coffeeChatMapper.upsertCoffeeChatSetting(userId, emailNotifyYn);

        return CoffeeChatSettingResponse.builder()
                .emailNotifyEnabled("Y".equals(emailNotifyYn))
                .build();
    }

    @Override
    @Transactional
    public CoffeeChatCreateResponse requestCoffeeChat(Long requesterUserId, CoffeeChatCreateRequest request) {
        return sendCoffeeChatMail(requesterUserId, request);
    }

    @Override
    @Transactional
    public CoffeeChatCreateResponse sendCoffeeChatMail(Long requesterUserId, CoffeeChatCreateRequest request) {
        Long receiverUserId = request.getReceiverUserId();

        if (requesterUserId.equals(receiverUserId)) {
            throw new BaseException(CoffeeChatErrorCode.SELF_REQUEST_NOT_ALLOWED);
        }

        CoffeeChatUserProfileDto requester = findProfileOrThrow(requesterUserId);
        CoffeeChatUserProfileDto receiver = findProfileOrThrow(receiverUserId);

        if (!"Y".equals(receiver.getEmailNotifyYn())) {
            throw new BaseException(CoffeeChatErrorCode.RECEIVER_NOTIFICATION_DISABLED);
        }

        if (!StringUtils.hasText(requester.getKcyResult()) || !StringUtils.hasText(receiver.getKcyResult())) {
            throw new BaseException(CoffeeChatErrorCode.KCY_RESULT_REQUIRED);
        }

        KcyType requesterType = KcyType.from(requester.getKcyResult());
        KcyType receiverType = KcyType.from(receiver.getKcyResult());
        KcyMatchGrade matchGrade = KcyCompatibilityPolicy.gradeOf(requesterType, receiverType);
        Long requestId = coffeeChatMapper.nextCoffeeChatRequestId();
        String message = normalizeMessage(request.getMessage(), requester.getName(), receiverType.getTitle(), matchGrade.getLabel());

        coffeeChatMapper.insertCoffeeChatRequest(
                requestId,
                requesterUserId,
                receiverUserId,
                requesterType.getCode(),
                receiverType.getCode(),
                matchGrade.name(),
                message,
                CoffeeChatRequestStatus.ACCEPTED.name()
        );
        competencyScoreService.applyActionScore(
                requesterUserId,
                CompetencyCalcType.KCY_MATCH_REQUEST,
                REFERENCE_TYPE_KCY_COFFEE_CHAT_REQUEST,
                requestId
        );

        MailSendStatus mailStatus = sendCoffeeChatMail(requestId, requester, receiver, receiverType, matchGrade, message);

        return CoffeeChatCreateResponse.builder()
                .requestId(requestId)
                .status(CoffeeChatRequestStatus.ACCEPTED.name())
                .mailStatus(mailStatus.name())
                .build();
    }

    @Override
    public List<CoffeeChatHistoryDto> getSentRequests(Long userId) {
        return coffeeChatMapper.findSentRequests(userId);
    }

    @Override
    public List<CoffeeChatHistoryDto> getReceivedRequests(Long userId) {
        return coffeeChatMapper.findReceivedRequests(userId);
    }

    @Override
    @Transactional
    public void acceptRequest(Long receiverUserId, Long requestId) {
        int updatedCount = coffeeChatMapper.updateReceivedRequestStatus(
                receiverUserId,
                requestId,
                CoffeeChatRequestStatus.ACCEPTED.name()
        );

        if (updatedCount == 0) {
            throw new BaseException(CoffeeChatErrorCode.REQUEST_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void rejectRequest(Long receiverUserId, Long requestId) {
        coffeeChatMapper.deleteMailLogsByReceivedRequest(receiverUserId, requestId);
        int deletedCount = coffeeChatMapper.deleteReceivedRequest(receiverUserId, requestId);

        if (deletedCount == 0) {
            throw new BaseException(CoffeeChatErrorCode.REQUEST_NOT_FOUND);
        }
    }

    private boolean isEmailNotifyEnabled(Long userId) {
        return "Y".equals(coffeeChatMapper.findEmailNotifyYnByUserId(userId));
    }

    private CoffeeChatUserProfileDto findProfileOrThrow(Long userId) {
        CoffeeChatUserProfileDto profile = coffeeChatMapper.findUserProfileForCoffeeChat(userId);

        if (profile == null) {
            throw new BaseException(CoffeeChatErrorCode.RECEIVER_NOT_FOUND);
        }

        return profile;
    }

    private String normalizeMessage(String message, String requesterName, String receiverKcyTitle, String gradeLabel) {
        if (StringUtils.hasText(message)) {
            return message.trim();
        }

        return requesterName + " 님이 " + receiverKcyTitle + " 성향의 " + gradeLabel + "로 커피챗 메일을 보냈습니다.";
    }

    private MailSendStatus sendCoffeeChatMail(
            Long requestId,
            CoffeeChatUserProfileDto requester,
            CoffeeChatUserProfileDto receiver,
            KcyType receiverType,
            KcyMatchGrade matchGrade,
            String message
    ) {
        String subject = "[H-LINKs] " + requester.getName() + " 님이 커피챗을 제안했습니다";
        String content = buildMailContent(requester, receiver, receiverType, matchGrade, message);
        JavaMailSender javaMailSender = javaMailSenderProvider.getIfAvailable();

        if (!mailEnabled || !StringUtils.hasText(mailHost) || javaMailSender == null) {
            insertMailLog(requestId, receiver, subject, content, MailSendStatus.SKIPPED, "메일 발송 설정이 비활성화되어 있습니다.");
            return MailSendStatus.SKIPPED;
        }

        if (!StringUtils.hasText(receiver.getEmail())) {
            insertMailLog(requestId, receiver, subject, content, MailSendStatus.SKIPPED, "수신자 이메일이 없습니다.");
            return MailSendStatus.SKIPPED;
        }

        Long mailLogId = insertMailLog(requestId, receiver, subject, content, MailSendStatus.PENDING, null);

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(mailFrom);
            mailMessage.setTo(receiver.getEmail());
            if (StringUtils.hasText(requester.getEmail())) {
                mailMessage.setReplyTo(requester.getEmail());
            }
            mailMessage.setSubject(subject);
            mailMessage.setText(content);

            javaMailSender.send(mailMessage);
            coffeeChatMapper.updateMailSendLogStatus(mailLogId, MailSendStatus.SENT.name(), null);
            return MailSendStatus.SENT;
        } catch (RuntimeException e) {
            log.warn("Coffee chat mail send failed. requestId={}, receiverUserId={}", requestId, receiver.getUserId(), e);
            coffeeChatMapper.updateMailSendLogStatus(mailLogId, MailSendStatus.FAILED.name(), e.getMessage());
            return MailSendStatus.FAILED;
        }
    }

    private Long insertMailLog(
            Long requestId,
            CoffeeChatUserProfileDto receiver,
            String subject,
            String content,
            MailSendStatus sendStatus,
            String errorMessage
    ) {
        Long mailLogId = coffeeChatMapper.nextMailLogId();

        coffeeChatMapper.insertMailSendLog(
                mailLogId,
                requestId,
                receiver.getUserId(),
                receiver.getEmail(),
                subject,
                content,
                sendStatus.name(),
                errorMessage
        );

        return mailLogId;
    }

    private String buildMailContent(
            CoffeeChatUserProfileDto requester,
            CoffeeChatUserProfileDto receiver,
            KcyType receiverType,
            KcyMatchGrade matchGrade,
            String message
    ) {
        return """
                %s 님, 안녕하세요.

                %s 님이 H-LINKs에서 커피챗을 제안했습니다.

                매칭 정보
                - 보낸 사람: %s / %s / %s
                - 받는 사람의 KCY 성향: %s
                - 매칭 등급: %s

                제안 메시지
                %s

                커피챗을 원하신다면 이 메일에 회신해 일정을 조율해 주세요.
                """.formatted(
                receiver.getName(),
                requester.getName(),
                requester.getName(),
                defaultText(requester.getDepartmentName(), "소속 부서 미정"),
                defaultText(requester.getPositionName(), "직급 미정"),
                receiverType.getTitle(),
                matchGrade.getLabel(),
                message
        );
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
