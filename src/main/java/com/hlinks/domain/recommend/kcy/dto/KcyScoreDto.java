package com.hlinks.domain.recommend.kcy.dto;

import com.hlinks.domain.recommend.kcy.type.KcyType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KcyScoreDto {

    private int actionScore;
    private int outlineScore;
    private int wideScore;
    private int deepScore;
    private int independentScore;
    private int corporateScore;
    private int prompterScore;
    private int manualScore;

    public KcyType toKcyType() {
        String code = getActionOrOutlineCode()
                + getWideOrDeepCode()
                + getIndependentOrCorporateCode()
                + getPrompterOrManualCode();

        return KcyType.from(code);
    }

    private String getActionOrOutlineCode() {
        return actionScore >= outlineScore ? "A" : "O";
    }

    private String getWideOrDeepCode() {
        return wideScore >= deepScore ? "W" : "D";
    }

    private String getIndependentOrCorporateCode() {
        return independentScore >= corporateScore ? "I" : "C";
    }

    private String getPrompterOrManualCode() {
        return prompterScore >= manualScore ? "P" : "M";
    }
}
