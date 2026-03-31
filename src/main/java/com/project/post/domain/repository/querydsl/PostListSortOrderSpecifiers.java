package com.project.post.domain.repository.querydsl;

import com.project.post.domain.entity.QPost;
import com.project.post.domain.enums.PostListSort;
import com.querydsl.core.types.OrderSpecifier;

import java.util.ArrayList;
import java.util.List;

public final class PostListSortOrderSpecifiers {

    private PostListSortOrderSpecifiers() {
    }

    public static OrderSpecifier<?>[] forPost(PostListSort sort, QPost post) {
        List<OrderSpecifier<?>> specifiers = new ArrayList<>();
        switch (sort) {
            case VIEWS -> {
                specifiers.add(post.viewCount.desc());
                specifiers.add(post.createdAt.desc());
            }
            case LIKES -> {
                specifiers.add(post.likeCount.desc());
                specifiers.add(post.createdAt.desc());
            }
            case POPULAR -> {
                var popularityScore = post.likeCount.add(post.viewCount);
                specifiers.add(popularityScore.desc());
                specifiers.add(post.createdAt.desc());
            }
            case LATEST -> specifiers.add(post.createdAt.desc());
            default -> throw new IllegalStateException("Unsupported sort: " + sort);
        }
        specifiers.add(post.id.desc());
        return specifiers.toArray(new OrderSpecifier<?>[0]);
    }
}
