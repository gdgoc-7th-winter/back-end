package com.project.post.application.service;

import com.project.post.application.dto.PostAttachmentRequest;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.PostAttachment;
import com.project.post.domain.repository.PostAttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PostAttachmentService {

    private final PostAttachmentRepository postAttachmentRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void replaceAttachments(@NonNull Post post, List<PostAttachmentRequest> dtos) {
        if (dtos == null) {
            return;
        }
        postAttachmentRepository.deleteByPostId(post.getId());
        if (dtos.isEmpty()) {
            return;
        }
        saveAttachments(post, dtos);
    }

    private void saveAttachments(@NonNull Post post, List<PostAttachmentRequest> dtos) {
        int order = 0;
        for (PostAttachmentRequest dto : dtos) {
            if (dto == null || dto.fileUrl() == null) {
                continue;
            }
            PostAttachment attachment = PostAttachment.of(
                    post,
                    dto.fileUrl(),
                    dto.fileName(),
                    dto.contentType(),
                    dto.fileSize(),
                    dto.sortOrder() >= 0 ? dto.sortOrder() : order++
            );
            postAttachmentRepository.save(Objects.requireNonNull(attachment));
        }
    }
}
