package com.hlinks.domain.recommend.kcy.service;

import com.hlinks.domain.recommend.kcy.dto.KcyAdaptiveRequest;
import com.hlinks.domain.recommend.kcy.dto.KcyAdaptiveResponse;
import com.hlinks.domain.recommend.kcy.dto.KcyScoreDto;
import com.hlinks.domain.recommend.kcy.dto.KcySubmitRequest;
import com.hlinks.domain.recommend.kcy.mapper.KcyMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KcyServiceImplTest {

    @Mock
    private KcyMapper kcyMapper;

    @InjectMocks
    private KcyServiceImpl kcyService;

    @Test
    @DisplayName("극단적 Prompter 스코어 시뮬레이션 및 테트리스 반영 로직 검증")
    void testExtremePrompterScenario() {
        // given
        KcySubmitRequest req = new KcySubmitRequest();
        req.setSelectedOptionIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L)); // 6개만 풂
        req.setAngerScoreTypes(Arrays.asList("PROMPTER", "PROMPTER")); // 하이라이터 점수 (총 4점)
        req.setTiebreakerBlocks(Arrays.asList("AI_COPILOT_1", "AI_COPILOT_2")); // 테트리스 AI 블록 (총 4점)
        req.setTimeTaken(10); // 15초 미만 -> ACTION 2점
        req.setFillRate(90); // 80% 이상 -> OUTLINE 2점

        KcyScoreDto mockScore = new KcyScoreDto();
        // 기본 6문제 푼거에 대한 점수 모의
        mockScore.addScore("PROMPTER", 6);
        when(kcyMapper.sumScoresByOptionIds(any())).thenReturn(mockScore);
        when(kcyMapper.updateUserKcyResult(any(), any())).thenReturn(1);

        // when
        KcyScoreDto finalScore = kcyService.submit(1L, req);

        // then
        // PROMPTER = 6(기본) + 4(하이라이터) + 4(블록) = 14
        assertThat(finalScore.getPrompterScore()).isEqualTo(14);
        assertThat(finalScore.getActionScore()).isEqualTo(2);
        assertThat(finalScore.getOutlineScore()).isEqualTo(2);
        assertThat(finalScore.toKcyType().getCode().contains("P")).isTrue();
    }
}
