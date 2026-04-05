package com.project.user.domain.repository;

public interface EmailAuthRepository {
    void saveAuthCode(String email, String code);
    String getAndDeleteAuthCode(String email);

    void setRegisterSession(String email);
    boolean hasRegisterSession(String email);
    void deleteRegisterSession(String email);
    boolean hasRecentRequest(String email);
    void saveSendLimit(String email, long limitSeconds);
    void deleteSendLimit(String email);
}
