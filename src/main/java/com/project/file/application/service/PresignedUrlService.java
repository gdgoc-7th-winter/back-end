package com.project.file.application.service;

import com.project.file.application.dto.PresignedUrlResponse;
import com.project.file.domain.enums.UploadType;
import com.project.user.domain.entity.User;

public interface PresignedUrlService {

    PresignedUrlResponse createPresignedUrl(UploadType uploadType, String contentType, Long referenceId, User uploader);
}
