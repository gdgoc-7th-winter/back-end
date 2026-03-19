package com.project.file.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * 업로드 타입에 따라 accessType, S3 prefix가 자동 결정된다.
 * 추후 private bucket 확장 시 enum 추가만 하면 된다.
 */
@Getter
@RequiredArgsConstructor
public enum UploadType {

    PROFILE_IMAGE("profile", AccessType.PUBLIC),
    POST_IMAGE("posts", AccessType.PUBLIC),
    ATTACHMENT("attachments", AccessType.PUBLIC),  // 강의/게시판 첨부파일 - 공개
    CHAT_IMAGE("chat", AccessType.PRIVATE);        // 채팅 이미지 - 비공개 (추후 구현)

    private final String prefix;
    private final AccessType accessType;

    private static final Set<UploadType> PUBLIC_TYPES = Set.of(PROFILE_IMAGE, POST_IMAGE, ATTACHMENT);
    private static final Set<UploadType> PRIVATE_TYPES = Set.of(CHAT_IMAGE);

    public boolean isPublic() {
        return PUBLIC_TYPES.contains(this);
    }

    public boolean isPrivate() {
        return PRIVATE_TYPES.contains(this);
    }
}
