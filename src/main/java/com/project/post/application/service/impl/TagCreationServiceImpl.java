package com.project.post.application.service.impl;

import com.project.post.application.service.TagCreationService;
import com.project.post.domain.entity.Tag;
import com.project.post.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagCreationServiceImpl implements TagCreationService {

    private final TagRepository tagRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Tag getOrCreate(@NonNull String name) {
        String normalizedName = normalizeName(name);
        return tagRepository.findByName(normalizedName)
                .orElseGet(() -> {
                    tagRepository.insertIfAbsent(normalizedName);
                    return tagRepository.findByName(normalizedName)
                            .orElseThrow(() -> new IllegalStateException("태그 생성에 실패했습니다: " + normalizedName));
                });
    }

    private String normalizeName(@NonNull String name) {
        Tag tag = new Tag(name);
        return tag.getName();
    }
}
