package com.project.file.application.dto;

import com.project.file.domain.constants.FileConstants;
import com.project.file.domain.enums.UploadType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PresignedUrlRequest(
        @NotNull(message = "REQUIRED")
        UploadType uploadType,

        @NotNull(message = "REQUIRED")
        @Pattern(regexp = FileConstants.CONTENT_TYPE_REGEX, message = "INVALID_FORMAT")
        String contentType,

        @NotNull(message = "REQUIRED")
        @Min(value = 1, message = "MIN_VALUE")
        Long referenceId
) {
}
