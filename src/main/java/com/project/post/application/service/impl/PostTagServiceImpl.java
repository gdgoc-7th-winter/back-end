package com.project.post.application.service.impl;

import com.project.post.application.service.PostTagService;
import com.project.post.application.service.TagCreationService;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostTagServiceImpl implements PostTagService {

    private final TagCreationService tagCreationService;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void replaceTags(@NonNull Post post, List<String> tagNames) {
        if (tagNames == null) {
            return;
        }
        if (tagNames.isEmpty()) {
            post.replaceTags(List.of());
            return;
        }
        List<Tag> tags = getOrCreateTags(tagNames);
        post.replaceTags(tags);
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
            result.add(tagCreationService.getOrCreate(name));
        }
        return result;
    }
}
