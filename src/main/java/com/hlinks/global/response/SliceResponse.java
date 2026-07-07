package com.hlinks.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SliceResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final boolean hasNext;

    public static <T> SliceResponse<T> of(List<T> content, int page, int size, boolean hasNext) {
        return new SliceResponse<>(content, page, size, hasNext);
    }
}
