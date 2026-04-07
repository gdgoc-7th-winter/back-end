package com.project.user.application.service.oauth;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.application.dto.OAuthAttributes;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RestClientOAuth2HttpClient implements OAuth2HttpClient {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final RestClient.Builder restClientBuilder;

    @Override
    public OAuthAttributes fetchUserInfo(String provider, String code, String redirectUri) {
        ClientRegistration reg = Optional.ofNullable(
                        clientRegistrationRepository.findByRegistrationId(provider.toLowerCase()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 provider: " + provider));

        RestClient restClient = restClientBuilder.build();
        String accessToken = exchangeCodeForToken(restClient, reg, code, redirectUri);
        Map<String, Object> userAttributes = fetchUserAttributes(restClient, reg, accessToken);
        String userNameAttr = reg.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        return OAuthAttributes.of(provider.toLowerCase(), userNameAttr, userAttributes);
    }

    private String exchangeCodeForToken(RestClient restClient, ClientRegistration reg,
                                         String code, String redirectUri) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        boolean isPost = ClientAuthenticationMethod.CLIENT_SECRET_POST.equals(reg.getClientAuthenticationMethod());
        if (isPost) {
            body.add("client_id", reg.getClientId());
            body.add("client_secret", reg.getClientSecret());
        }

        var spec = restClient.post()
                .uri(reg.getProviderDetails().getTokenUri())
                .header("Content-Type", "application/x-www-form-urlencoded");

        if (!isPost) {
            String credentials = reg.getClientId() + ":" + reg.getClientSecret();
            String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            spec = spec.header("Authorization", "Basic " + encoded);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> response = spec.body(body).retrieve().body(Map.class);

        return Optional.ofNullable(response)
                .map(r -> (String) r.get("access_token"))
                .orElseThrow(() -> new BusinessException(ErrorCode.OAUTH_PROVIDER_ERROR, "액세스 토큰을 받아올 수 없습니다."));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchUserAttributes(RestClient restClient, ClientRegistration reg,
                                                     String accessToken) {
        return restClient.get()
                .uri(reg.getProviderDetails().getUserInfoEndpoint().getUri())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);
    }
}
