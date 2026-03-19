package com.project.file.application.service;

import com.project.file.application.dto.FileCompleteRequest;
import com.project.file.application.dto.FileCompleteResponse;
import com.project.user.domain.entity.User;

public interface FileUploadService {

    FileCompleteResponse completeUpload(FileCompleteRequest request, User uploader);
}
