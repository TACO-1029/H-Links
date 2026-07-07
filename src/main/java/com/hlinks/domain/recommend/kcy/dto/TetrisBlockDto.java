package com.hlinks.domain.recommend.kcy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TetrisBlockDto {
    private String id;
    private String name;
    private String theme;
    private String type;
    private int width;
    private int height;
    private List<List<Integer>> layout;
    private String description; // 기술/방법론 한줄 설명
}
