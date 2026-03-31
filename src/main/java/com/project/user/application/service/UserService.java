package com.project.user.application.service;

import com.project.user.application.dto.EarnScoreResult;
import com.project.user.application.dto.response.ProfileResponse;
import com.project.user.domain.enums.Authority;
import com.project.user.presentation.dto.request.PasswordUpdateRequest;
import com.project.user.presentation.dto.request.ProfilePatchRequest;
import com.project.user.presentation.dto.request.ProfileUpdateRequest;
import com.project.user.presentation.dto.request.SignUpRequest;
import jakarta.servlet.http.HttpSession;

public interface UserService {
    public void signUp(SignUpRequest request);
    public void login(String email, String password);
    public void logout(HttpSession session);
    public void updateProfile(Long id, ProfileUpdateRequest request);
    public void updateSecurityContext(Long id);
    public void changePassword(Long id, PasswordUpdateRequest request);
    public ProfileResponse getUserProfile(Long id);
    EarnScoreResult earnAScore(Long id, String scoreCode, Long referenceId);
    public void linkSocialAccount(Long userId, String provider, String email, String providerId);
    public void patchProfile(Long id, ProfilePatchRequest request);
    public void deleteUser(Long id);
    public void grantAuthority(Long userId, Authority authority);
}
