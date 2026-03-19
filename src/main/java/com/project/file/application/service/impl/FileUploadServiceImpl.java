package com.project.file.application.service.impl;

import com.project.file.application.dto.FileCompleteRequest;
import com.project.file.application.dto.FileCompleteResponse;
import com.project.file.config.CloudFrontProperties;
import com.project.file.config.FileValidationConfig;
import com.project.file.config.S3Properties;
import com.project.file.domain.constants.FileConstants;
import com.project.file.domain.entity.FileMetadata;
import com.project.file.domain.enums.AccessType;
import com.project.file.domain.enums.UploadType;
import com.project.file.domain.repository.FileMetadataRepository;
import com.project.file.application.service.FileUploadService;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.domain.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    private final S3Client s3Client;
    private final S3Properties s3Properties;
    private final CloudFrontProperties cloudFrontProperties;
    private final FileValidationConfig fileValidationConfig;
    private final FileMetadataRepository fileMetadataRepository;

    public FileUploadServiceImpl(S3Client s3Client, S3Properties s3Properties,
                                 CloudFrontProperties cloudFrontProperties,
                                 FileValidationConfig fileValidationConfig,
                                 FileMetadataRepository fileMetadataRepository) {
        this.s3Client = s3Client;
        this.s3Properties = s3Properties;
        this.cloudFrontProperties = cloudFrontProperties;
        this.fileValidationConfig = fileValidationConfig;
        this.fileMetadataRepository = fileMetadataRepository;
    }

    @Override
    @Transactional
    public FileCompleteResponse completeUpload(FileCompleteRequest request, User uploader) {
        validateContentType(request.contentType());
        validateSize(request.size());
        validateObjectKeyPrefix(request.objectKey(), request.uploadType());
        validateUploadType(request.uploadType());

        String bucket = resolveBucket(request.uploadType());
        verifyS3ObjectExists(bucket, request.objectKey(), request.contentType(), request.size());

        FileMetadata metadata = FileMetadata.of(
                bucket,
                request.objectKey(),
                request.uploadType(),
                request.uploadType().getAccessType(),
                request.contentType(),
                request.size(),
                uploader
        );
        FileMetadata saved = fileMetadataRepository.save(metadata);

        String resolvedUrl = resolveUrl(request.uploadType().getAccessType(), request.objectKey());

        return new FileCompleteResponse(
                saved.getId(),
                saved.getObjectKey(),
                saved.getAccessType(),
                resolvedUrl
        );
    }

    private void validateContentType(String contentType) {
        if (!fileValidationConfig.isAllowedContentType(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_CONTENT_TYPE);
        }
    }

    private void validateSize(Long size) {
        if (size > s3Properties.getMaxFileSizeBytes()) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    private void validateObjectKeyPrefix(String objectKey, UploadType uploadType) {
        String expectedPrefix = uploadType.getPrefix() + "/";
        if (!objectKey.startsWith(expectedPrefix)) {
            throw new BusinessException(ErrorCode.INVALID_OBJECT_KEY);
        }
        String[] parts = objectKey.split("/");
        if (parts.length < FileConstants.OBJECT_KEY_MIN_PARTS) {
            throw new BusinessException(ErrorCode.INVALID_OBJECT_KEY);
        }
    }

    private void validateUploadType(UploadType uploadType) {
        if (uploadType.isPrivate()) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_UPLOAD_TYPE);
        }
    }

    private String resolveBucket(UploadType uploadType) {
        if (uploadType.isPublic()) {
            return s3Properties.getBucketNamePublic();
        }
        throw new BusinessException(ErrorCode.UNSUPPORTED_UPLOAD_TYPE);
    }

    private void verifyS3ObjectExists(String bucket, String objectKey, String expectedContentType, Long expectedSize) {
        try {
            var headRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            var response = s3Client.headObject(headRequest);

            if (expectedContentType != null && response.contentType() != null
                    && !response.contentType().equalsIgnoreCase(expectedContentType)) {
                throw new BusinessException(ErrorCode.INVALID_OBJECT_KEY);
            }
            if (expectedSize != null && response.contentLength() != null
                    && !response.contentLength().equals(expectedSize)) {
                throw new BusinessException(ErrorCode.INVALID_OBJECT_KEY);
            }
        } catch (NoSuchKeyException e) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND_IN_S3);
        } catch (S3Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String resolveUrl(AccessType accessType, String objectKey) {
        if (accessType != AccessType.PUBLIC) {
            return null;  // PRIVATE는 Signed URL 필요 (추후 구현)
        }
        String domain = cloudFrontProperties.getDomain();
        if (domain == null || domain.isBlank()) {
            return null;
        }
        String base = domain.endsWith("/") ? domain : domain + "/";
        return base + objectKey;
    }
}
