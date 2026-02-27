package com.project.user.domain.repository.impl;

import com.project.user.domain.repository.EmailAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RedisEmailAuthRepository implements EmailAuthRepository {
    private final StringRedisTemplate redisTemplate;
    private static final long AUTH_CODE_TTL = 5; // 5분
    private static final long SESSION_TTL = 30;  // 30분

    // 이메일 인증 번호 저장 로직
    @Override
    public void saveAuthCode(String email, String code) {
        redisTemplate.opsForValue().set(
                "AUTH_CODE:" + email,
                code,
                Duration.ofMinutes(AUTH_CODE_TTL)
        );
    }

    @Override
    public String getAuthCode(String email) {
        return redisTemplate.opsForValue().get("AUTH_CODE:" + email);
    }

    @Override
    public void deleteAuthcode(String email) {
        redisTemplate.delete("AUTH_CODE:" + email);
    }

    // 회원가입 권한 세션 (30분) 정보 Redis DB 저장 로직
    @Override
    public void setRegisterSession(String email) {
        redisTemplate.opsForValue().set(
                "REGISTER_SESSION:" + email,
                "VALIDATED",
                Duration.ofMinutes(SESSION_TTL)
        );
    }
}
