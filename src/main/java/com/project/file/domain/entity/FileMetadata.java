package com.project.file.domain.entity;

import com.project.file.domain.enums.AccessType;
import com.project.file.domain.enums.UploadType;
import com.project.user.domain.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "file_metadata")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long id;

    @Column(name = "bucket", nullable = false, length = 255)
    private String bucket;

    @Column(name = "object_key", nullable = false, columnDefinition = "TEXT")
    private String objectKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_type", nullable = false, length = 50)
    private UploadType uploadType;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false, length = 20)
    private AccessType accessType;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size", nullable = false)
    private Long size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    private FileMetadata(String bucket, String objectKey, UploadType uploadType, AccessType accessType,
                        String contentType, Long size, User uploader) {
        this.bucket = bucket;
        this.objectKey = objectKey;
        this.uploadType = uploadType;
        this.accessType = accessType;
        this.contentType = contentType;
        this.size = size;
        this.uploader = uploader;
    }

    public static FileMetadata of(String bucket, String objectKey, UploadType uploadType, AccessType accessType,
                                  String contentType, Long size, User uploader) {
        return new FileMetadata(bucket, objectKey, uploadType, accessType, contentType, size, uploader);
    }
}
