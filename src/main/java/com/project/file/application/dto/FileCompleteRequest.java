package com.project.file.application.dto;

import com.project.file.domain.constants.FileConstants;
import com.project.file.domain.enums.UploadType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FileCompleteRequest(
        @NotNull(message = "REQUIRED")
        @Size(min = 1, max = FileConstants.OBJECT_KEY_MAX_LENGTH)
        String objectKey,

        @NotNull(message = "REQUIRED")
        UploadType uploadType,

        @NotNull(message = "REQUIRED")
        @Min(value = 1, message = "MIN_VALUE")
        @Max(value = FileConstants.MAX_FILE_SIZE_BYTES, message = "MAX_VALUE")
        Long size,

        @NotNull(message = "REQUIRED")
        @Pattern(regexp = FileConstants.CONTENT_TYPE_REGEX, message = "INVALID_FORMAT")
        String contentType
) {
}
