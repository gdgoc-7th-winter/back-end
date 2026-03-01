package com.project.user.domain.repository;

public interface EmailAuthRepository {
    void saveAuthCode(String email, String code);
    String getAuthCode(String email);
    void deleteAuthCode(String email);
    void setRegisterSession(String email);

    boolean hasRecentRequest(String email);
    void saveSendLimit(String email, long limitSeconds);
}
