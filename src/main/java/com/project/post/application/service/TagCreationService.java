package com.project.post.application.service;

import com.project.post.domain.entity.Tag;
import com.project.post.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagCreationService {

    private final TagRepository tagRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public Tag getOrCreate(@NonNull String name) {
        return tagRepository.findByName(name)
                .orElseGet(() -> {
                    tagRepository.insertIfAbsent(name);
                    return tagRepository.findByName(name)
                            .orElseThrow(() -> new IllegalStateException("태그 생성에 실패했습니다: " + name));
                });
    }
}
