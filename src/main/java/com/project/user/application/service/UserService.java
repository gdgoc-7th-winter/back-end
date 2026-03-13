package com.project.user.application.service;

import com.project.contribution.domain.entity.ContributionScore;
import com.project.user.application.dto.response.ProfileResponse;
import com.project.user.domain.entity.User;
import com.project.user.presentation.dto.request.LoginRequest;
import com.project.user.presentation.dto.request.ProfileUpdateRequest;
import com.project.user.presentation.dto.request.SignUpRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public interface UserService {
    public void signUp(SignUpRequest request);
    public void login(LoginRequest request, HttpSession session, HttpServletRequest servletRequest);
    public void completeInitialProfile(Long id, ProfileUpdateRequest request, HttpSession session);
    public void updateSecurityContext(Long id);
    public ProfileResponse getUserProfile(Long id);
    public User earnAScore(Long id, ContributionScore contributionScore);
}
