package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
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

    private static final int MAX_ATTACHMENTS = 10;

    private final PostAttachmentRepository postAttachmentRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void replaceAttachments(@NonNull Post post, List<PostAttachmentRequest> dtos) {
        if (dtos == null) {
            return;
        }
        if (dtos.size() > MAX_ATTACHMENTS) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "첨부파일은 최대 10개까지 등록 가능합니다.");
        }
        postAttachmentRepository.deleteByPostId(post.getId());
        if (dtos.isEmpty()) {
            return;
        }
        saveAttachments(post, dtos);
    }

    private void saveAttachments(@NonNull Post post, List<PostAttachmentRequest> dtos) {
        int sortOrder = 0;
        for (PostAttachmentRequest dto : dtos) {
            if (dto == null || dto.fileUrl() == null) {
                continue;
            }
            int order = dto.sortOrder() >= 0 ? dto.sortOrder() : sortOrder++;
            PostAttachment attachment = PostAttachment.of(
                    post,
                    dto.fileUrl(),
                    dto.fileName(),
                    dto.contentType(),
                    dto.fileSize(),
                    order
            );
            postAttachmentRepository.save(Objects.requireNonNull(attachment));
        }
    }
}
