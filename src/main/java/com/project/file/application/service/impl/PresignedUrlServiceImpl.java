package com.project.file.application.service.impl;

import com.project.file.application.dto.PresignedUrlResponse;
import com.project.file.config.FileValidationConfig;
import com.project.file.config.S3Properties;
import com.project.file.domain.constants.FileConstants;
import com.project.file.domain.enums.UploadType;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.file.application.service.PresignedUrlService;
import com.project.user.domain.entity.User;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.util.UUID;

@Service
public class PresignedUrlServiceImpl implements PresignedUrlService {

    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;
    private final FileValidationConfig fileValidationConfig;

    public PresignedUrlServiceImpl(S3Presigner s3Presigner, S3Properties s3Properties,
                                   FileValidationConfig fileValidationConfig) {
        this.s3Presigner = s3Presigner;
        this.s3Properties = s3Properties;
        this.fileValidationConfig = fileValidationConfig;
    }

    @Override
    public PresignedUrlResponse createPresignedUrl(UploadType uploadType, String contentType,
                                                   Long referenceId, User uploader) {
        validateContentType(contentType);
        validateUploadType(uploadType);
        validateReferenceId(uploadType, referenceId, uploader);

        int expireSeconds = clampExpireSeconds(s3Properties.getPresignedUrlExpireSeconds());
        String objectKey = generateObjectKey(uploadType, contentType, referenceId, uploader.getId());
        String bucket = resolveBucket(uploadType);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(java.time.Duration.ofSeconds(expireSeconds))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        URL uploadUrl = presignedRequest.url();

        return new PresignedUrlResponse(
                uploadUrl.toString(),
                objectKey,
                uploadType.getAccessType(),
                expireSeconds
        );
    }

    private void validateContentType(String contentType) {
        if (!fileValidationConfig.isAllowedContentType(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_CONTENT_TYPE);
        }
    }

    private void validateUploadType(UploadType uploadType) {
        if (uploadType.isPrivate()) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_UPLOAD_TYPE);
        }
    }

    private void validateReferenceId(UploadType uploadType, Long referenceId, User uploader) {
        if (uploadType == UploadType.PROFILE_IMAGE && !referenceId.equals(uploader.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private int clampExpireSeconds(int seconds) {
        return Math.max(FileConstants.PRESIGNED_URL_MIN_EXPIRE_SECONDS,
                Math.min(FileConstants.PRESIGNED_URL_MAX_EXPIRE_SECONDS, seconds));
    }

    private String generateObjectKey(UploadType uploadType, String contentType, Long referenceId, Long uploaderId) {
        String ext = fileValidationConfig.getExtensionForContentType(contentType);
        if (ext == null) {
            throw new BusinessException(ErrorCode.INVALID_CONTENT_TYPE);
        }
        String uuid = UUID.randomUUID().toString();
        long id = uploadType == UploadType.ATTACHMENT ? uploaderId : referenceId;
        return FileConstants.OBJECT_KEY_FORMAT.formatted(uploadType.getPrefix(), id, uuid, ext);
    }

    private String resolveBucket(UploadType uploadType) {
        if (uploadType.isPublic()) {
            return s3Properties.getBucketNamePublic();
        }
        throw new BusinessException(ErrorCode.UNSUPPORTED_UPLOAD_TYPE);
    }
}
