package com.project.file.domain.repository;

import com.project.file.domain.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    Optional<FileMetadata> findByBucketAndObjectKey(String bucket, String objectKey);
}
