package com.project.file.presentation.controller;

import com.project.file.application.dto.FileCompleteRequest;
import com.project.file.application.dto.FileCompleteResponse;
import com.project.file.application.dto.PresignedUrlRequest;
import com.project.file.application.dto.PresignedUrlResponse;
import com.project.file.application.service.FileUploadService;
import com.project.file.application.service.PresignedUrlService;
import com.project.global.annotation.CurrentUser;
import com.project.global.response.CommonResponse;
import com.project.user.domain.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final PresignedUrlService presignedUrlService;
    private final FileUploadService fileUploadService;

    @PostMapping("/presigned-url")
    public ResponseEntity<CommonResponse<PresignedUrlResponse>> createPresignedUrl(
            @RequestBody @Valid PresignedUrlRequest request,
            @CurrentUser User user) {
        PresignedUrlResponse response = presignedUrlService.createPresignedUrl(
                request.uploadType(),
                request.contentType(),
                request.referenceId(),
                user
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.ok(response));
    }

    @PostMapping("/complete")
    public ResponseEntity<CommonResponse<FileCompleteResponse>> completeUpload(
            @RequestBody @Valid FileCompleteRequest request,
            @CurrentUser User user) {
        FileCompleteResponse response = fileUploadService.completeUpload(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.ok(response));
    }
}
