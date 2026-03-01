package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.PostTag;
import com.project.post.domain.entity.Tag;
import com.project.post.domain.repository.PostTagRepository;
import com.project.post.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostTagService {

    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void replaceTags(@NonNull Post post, List<String> tagNames) {
        if (tagNames == null) {
            return;
        }
        postTagRepository.deleteByPostId(post.getId());
        if (tagNames.isEmpty()) {
            return;
        }
        List<Tag> tags = getOrCreateTags(tagNames);
        for (Tag tag : tags) {
            postTagRepository.save(new PostTag(post, tag));
        }
    }

    private List<Tag> getOrCreateTags(List<String> tagNames) {
        Set<String> uniqueNames = new LinkedHashSet<>();
        for (String name : tagNames) {
            if (name == null || name.isBlank()) {
                continue;
            }
            uniqueNames.add(name.trim());
        }

        List<Tag> result = new ArrayList<>();
        for (String name : uniqueNames) {
            result.add(getOrCreateTag(name));
        }
        return result;
    }

    private Tag getOrCreateTag(String name) {
        return tagRepository.findByName(name)
                .orElseGet(() -> {
                    try {
                        return tagRepository.save(new Tag(name));
                    } catch (DataIntegrityViolationException ex) {
                        return tagRepository.findByName(name)
                                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "태그 생성에 실패했습니다."));
                    }
                });
    }
}
