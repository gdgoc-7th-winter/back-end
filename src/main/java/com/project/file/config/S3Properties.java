package com.project.file.config;

import com.project.file.domain.constants.FileConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cloud.aws.s3")
public class S3Properties {

    private String accessKeyId;
    private String secretAccessKey;
    private String bucketNamePublic;
    private String region;
    private int presignedUrlExpireSeconds = 600;
    private long maxFileSizeBytes = FileConstants.MAX_FILE_SIZE_BYTES;

    public String getBucketNamePublic() {
        return bucketNamePublic;
    }

    public void setBucketNamePublic(String bucketNamePublic) {
        this.bucketNamePublic = bucketNamePublic;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getPresignedUrlExpireSeconds() {
        return presignedUrlExpireSeconds;
    }

    public void setPresignedUrlExpireSeconds(int presignedUrlExpireSeconds) {
        this.presignedUrlExpireSeconds = presignedUrlExpireSeconds;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }
}
