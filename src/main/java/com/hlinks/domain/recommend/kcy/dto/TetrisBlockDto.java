package com.hlinks.domain.recommend.kcy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TetrisBlockDto {
    private String id;
    private String name;
    private String theme; // "AI", "INFRA", "BACKEND", "FRONTEND", "SWE"
    private String type; // "PROMPTER", "MANUAL", "CORPORATE", "STARTUP", etc.
    private int width;
    private int height;
    private java.util.List<java.util.List<Integer>> layout; // e.g. [[1,1,0],[0,1,1]] for Z-shape
}
