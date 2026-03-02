package com.project.post.application.service;

import org.springframework.lang.NonNull;
import java.util.List;
import com.project.post.domain.entity.Post;

public interface PostTagService {

    void replaceTags(@NonNull Post post, List<String> tagNames);
}
