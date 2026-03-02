package com.project.post.application.service;

import org.springframework.lang.NonNull;
import com.project.post.domain.entity.Tag;

public interface TagCreationService {

    Tag getOrCreate(@NonNull String name);
}
