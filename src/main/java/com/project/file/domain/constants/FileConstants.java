package com.project.file.domain.constants;

import java.util.Map;
import java.util.Set;

public final class FileConstants {

    private FileConstants() {
    }

    public static final String CONTENT_TYPE_REGEX =
            "image/(jpeg|png|webp|gif)|application/pdf|application/msword|"
                    + "application/vnd\\.openxmlformats-officedocument\\.wordprocessingml\\.document";

    public static final Map<String, Set<String>> CONTENT_TYPE_TO_EXTENSIONS = Map.of(
            "image/jpeg", Set.of("jpg", "jpeg"),
            "image/png", Set.of("png"),
            "image/webp", Set.of("webp"),
            "image/gif", Set.of("gif"),
            "application/pdf", Set.of("pdf"),
            "application/msword", Set.of("doc"),
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", Set.of("docx")
    );

    /** contentType → object key 생성 시 사용할 대표 확장자 */
    public static final Map<String, String> CONTENT_TYPE_TO_PRIMARY_EXTENSION = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif",
            "application/pdf", "pdf",
            "application/msword", "doc",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"
    );

    public static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;

    public static final int OBJECT_KEY_MIN_PARTS = 3;
    public static final String OBJECT_KEY_FORMAT = "%s/%d/%s.%s";
    public static final int OBJECT_KEY_MAX_LENGTH = 1024;

    public static final int PRESIGNED_URL_MIN_EXPIRE_SECONDS = 300;
    public static final int PRESIGNED_URL_MAX_EXPIRE_SECONDS = 600;
}
