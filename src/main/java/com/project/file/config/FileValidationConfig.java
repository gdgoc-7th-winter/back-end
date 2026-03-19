package com.project.file.config;

import com.project.file.domain.constants.FileConstants;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class FileValidationConfig {

    public boolean isAllowedContentType(String contentType) {
        return contentType != null
                && FileConstants.CONTENT_TYPE_TO_EXTENSIONS.containsKey(contentType.toLowerCase());
    }

    public boolean isExtensionAllowedForContentType(String contentType, String extension) {
        if (contentType == null || extension == null) {
            return false;
        }
        Set<String> allowed = FileConstants.CONTENT_TYPE_TO_EXTENSIONS.get(contentType.toLowerCase());
        return allowed != null && allowed.contains(extension.toLowerCase());
    }

    public String getExtensionForContentType(String contentType) {
        if (contentType == null) {
            return null;
        }
        Set<String> extensions = FileConstants.CONTENT_TYPE_TO_EXTENSIONS.get(contentType.toLowerCase());
        return extensions != null ? extensions.iterator().next() : null;
    }

    public Set<String> getAllowedContentTypes() {
        return FileConstants.CONTENT_TYPE_TO_EXTENSIONS.keySet();
    }
}
