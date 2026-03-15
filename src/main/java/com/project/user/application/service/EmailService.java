package com.project.user.application.service;

public interface EmailService {
    public void validateHufsEmail(String email);
    public void sendAuthEmail(String email);
    public void verifyCode(String email, String authCode);
}
