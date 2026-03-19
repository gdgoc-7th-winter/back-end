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
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    private final S3Client s3Client;
    private final S3Properties s3Properties;
    private final CloudFrontProperties cloudFrontProperties;
    private final FileValidationConfig fileValidationConfig;
    private final FileMetadataRepository fileMetadataRepository;
    private final TransactionTemplate transactionTemplate;

    public FileUploadServiceImpl(S3Client s3Client, S3Properties s3Properties,
                                 CloudFrontProperties cloudFrontProperties,
                                 FileValidationConfig fileValidationConfig,
                                 FileMetadataRepository fileMetadataRepository,
                                 TransactionTemplate transactionTemplate) {
        this.s3Client = s3Client;
        this.s3Properties = s3Properties;
        this.cloudFrontProperties = cloudFrontProperties;
        this.fileValidationConfig = fileValidationConfig;
        this.fileMetadataRepository = fileMetadataRepository;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public FileCompleteResponse completeUpload(FileCompleteRequest request, User uploader) {
        validateContentType(request.contentType());
        validateSize(request.size());
        validateObjectKey(request.objectKey(), request.uploadType(), request.contentType());
        validateUploadType(request.uploadType());

        String bucket = resolveBucket(request.uploadType());
        verifyS3ObjectExists(bucket, request.objectKey(), request.contentType(), request.size());

        FileMetadata saved = transactionTemplate.execute(status -> {
            try {
                return fileMetadataRepository.findByBucketAndObjectKey(bucket, request.objectKey())
                        .orElseGet(() -> {
                            FileMetadata metadata = FileMetadata.of(
                                    bucket,
                                    request.objectKey(),
                                    request.uploadType(),
                                    request.uploadType().getAccessType(),
                                    request.contentType(),
                                    request.size(),
                                    uploader
                            );
                            return fileMetadataRepository.save(metadata);
                        });
            } catch (DataIntegrityViolationException e) {
                return fileMetadataRepository.findByBucketAndObjectKey(bucket, request.objectKey())
                        .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));
            }
        });

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

    private void validateObjectKey(String objectKey, UploadType uploadType, String contentType) {
        String expectedPrefix = uploadType.getPrefix() + "/";
        if (!objectKey.startsWith(expectedPrefix)) {
            throw new BusinessException(ErrorCode.INVALID_OBJECT_KEY);
        }
        String[] parts = objectKey.split("/");
        if (parts.length < FileConstants.OBJECT_KEY_MIN_PARTS) {
            throw new BusinessException(ErrorCode.INVALID_OBJECT_KEY);
        }
        String extension = extractExtension(objectKey);
        if (extension == null || !fileValidationConfig.isExtensionAllowedForContentType(contentType, extension)) {
            throw new BusinessException(ErrorCode.INVALID_OBJECT_KEY);
        }
    }

    private String extractExtension(String objectKey) {
        int lastDot = objectKey.lastIndexOf('.');
        if (lastDot == -1 || lastDot == objectKey.length() - 1) {
            return null;
        }
        return objectKey.substring(lastDot + 1).toLowerCase();
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
                throw new BusinessException(ErrorCode.FILE_METADATA_MISMATCH);
            }
            if (expectedSize != null && response.contentLength() != null
                    && !response.contentLength().equals(expectedSize)) {
                throw new BusinessException(ErrorCode.FILE_METADATA_MISMATCH);
            }
        } catch (NoSuchKeyException e) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND_IN_S3);
        } catch (S3Exception e) {
            log.warn("S3 headObject failed: bucket={}, key={}", bucket, objectKey, e);
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
