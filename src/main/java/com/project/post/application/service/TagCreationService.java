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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Tag createTag(@NonNull String name) {
        return tagRepository.save(new Tag(name));
    }
}
