package com.project.file.application.dto;

import com.project.file.domain.enums.AccessType;

public record PresignedUrlResponse(
        String uploadUrl,
        String objectKey,
        AccessType accessType,
        int expiresIn
) {
}
