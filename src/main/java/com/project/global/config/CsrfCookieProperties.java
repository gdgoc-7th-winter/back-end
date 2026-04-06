package com.project.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "csrf.cookie")
public record CsrfCookieProperties(

        String domain,
        boolean secure,
        String sameSite,
        String path
) {
    public CsrfCookieProperties {
        if (sameSite == null || sameSite.isBlank()) {
            sameSite = "Lax";
        }
        if (path == null || path.isBlank()) {
            path = "/";
        }
    }

    public boolean hasDomain() {
        return domain != null && !domain.isBlank();
    }

    public String domainForSetCookie() {
        if (!hasDomain()) {
            return null;
        }
        String d = domain.trim();
        if (d.startsWith(".")) {
            return d.substring(1);
        }
        return d;
    }
}
