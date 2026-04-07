package com.project.user.application.service.oauth;

import com.project.user.application.dto.OAuthAttributes;

public interface OAuth2HttpClient {

    OAuthAttributes fetchUserInfo(String provider, String code, String redirectUri);
}
