package com.project.user.application.dto;

import jakarta.validation.constraints.NotBlank;

public record OAuthCodeRequest(
        @NotBlank String code,
        @NotBlank String redirectUri
) {}
