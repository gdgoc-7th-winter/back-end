package com.project.file.application.dto;

import com.project.file.domain.enums.AccessType;

public record FileCompleteResponse(
        Long fileId,
        String objectKey,
        AccessType accessType,
        String resolvedUrl
) {
}
