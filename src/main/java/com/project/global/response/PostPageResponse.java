package com.project.global.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PostPageResponse<T>(
        List<T> posts,
        PageMeta meta
) {

    public static <T> PostPageResponse<T> of(Page<T> page) {
        return new PostPageResponse<>(
                page.getContent(),
                new PageMeta(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages()
                )
        );
    }
}
