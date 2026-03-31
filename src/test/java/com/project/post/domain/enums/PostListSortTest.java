package com.project.post.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PostListSortTest {

    @ParameterizedTest
    @CsvSource({
            "popular, POPULAR",
            "POPULAR, POPULAR",
            "hot, POPULAR",
            "likes, LIKES",
            "views, VIEWS",
            "latest, LATEST",
            "'', LATEST",
    })
    @DisplayName("from(String)은 요청 파라미터를 정렬 enum으로 매핑한다")
    void fromMapsRequestParam(String input, PostListSort expected) {
        assertThat(PostListSort.from(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("null이면 최신순")
    void fromNullIsLatest() {
        assertThat(PostListSort.from(null)).isEqualTo(PostListSort.LATEST);
    }
}
