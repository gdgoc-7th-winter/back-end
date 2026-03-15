package com.project.post.application.service;

import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;

public interface PostTagQueryService {

    Map<Long, List<String>> getTagNamesByPostIds(@NonNull List<Long> postIds);
}
