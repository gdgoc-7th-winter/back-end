package com.project.post.application.service.impl;

import com.project.post.application.service.PostTagQueryService;
import com.project.post.domain.entity.PostTag;
import com.project.post.domain.repository.PostTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostTagQueryServiceImpl implements PostTagQueryService {

    private final PostTagRepository postTagRepository;

    @Override
    public Map<Long, List<String>> getTagNamesByPostIds(@NonNull List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }

        List<PostTag> postTags = postTagRepository.findByPostIdIn(postIds);
        Map<Long, List<String>> tagsByPostId = new HashMap<>();

        for (PostTag postTag : postTags) {
            Long postId = postTag.getPost().getId();
            String tagName = postTag.getTag() == null ? null : postTag.getTag().getName();
            if (tagName == null) {
                continue;
            }
            tagsByPostId
                    .computeIfAbsent(postId, id -> new ArrayList<>())
                    .add(tagName);
        }

        tagsByPostId.replaceAll((id, names) -> names.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList());

        return tagsByPostId;
    }
}
