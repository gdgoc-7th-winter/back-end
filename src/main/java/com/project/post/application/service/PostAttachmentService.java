package com.project.post.application.service;

import com.project.post.application.dto.PostAttachmentRequest;
import org.springframework.lang.NonNull;
import java.util.List;
import com.project.post.domain.entity.Post;

public interface PostAttachmentService {

    void replaceAttachments(@NonNull Post post, List<PostAttachmentRequest> dtos);
}
