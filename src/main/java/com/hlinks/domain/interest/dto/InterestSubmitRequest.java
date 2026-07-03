package com.hlinks.domain.interest.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InterestSubmitRequest {

    private List<Long> skillIds;
}
