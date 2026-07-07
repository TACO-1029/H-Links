package com.hlinks.domain.recommend.kcy.service;

import com.hlinks.domain.recommend.kcy.dto.KcyScoreDto;
import com.hlinks.domain.recommend.kcy.dto.KcySubmitRequest;
import com.hlinks.domain.recommend.kcy.mapper.KcyMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

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
        req.setSelectedOptionIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L)); // 6개 풂
        req.setAngerScoreTypes(Arrays.asList("PROMPTER", "PROMPTER")); // 하이라이터 점수 (개당 1점, 총 2점)
        req.setTiebreakerBlocks(Arrays.asList("blk-ai-4", "blk-ai-4")); // 테트리스 AI 블록 (개당 1점, 총 2점)
        req.setTimeTaken(10); // 15초 미만 -> ACTION 1점
        req.setFillRate(90); // 80% 이상 -> OUTLINE 1점

        KcyScoreDto mockScore = new KcyScoreDto();
        mockScore.addScore("PROMPTER", 6);
        when(kcyMapper.sumScoresByOptionIds(any())).thenReturn(mockScore);
        when(kcyMapper.updateUserKcyResult(any(), any())).thenReturn(1);

        // when
        KcyScoreDto finalScore = kcyService.submit(1L, req);

        // then
        // PROMPTER = 6(기본) + 0(Base) + 2(하이라이터) + 2(블록) = 10
        assertThat(finalScore.getPrompterScore()).isEqualTo(10);
        // Base 0 + 1(보너스) = 1
        assertThat(finalScore.getActionScore()).isEqualTo(1);
        assertThat(finalScore.getOutlineScore()).isEqualTo(1);
        assertThat(finalScore.toKcyType().getCode().contains("P")).isTrue();
    }
}
