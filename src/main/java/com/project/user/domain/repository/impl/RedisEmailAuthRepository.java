package com.project.user.domain.repository.impl;

import com.project.user.domain.repository.EmailAuthRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class RedisEmailAuthRepository implements EmailAuthRepository {
    private final StringRedisTemplate redisTemplate;
    private final long authCodeTtl;
    private final long sessionTtl;

    public RedisEmailAuthRepository(
            StringRedisTemplate redisTemplate,
            @Value("${spring.data.redis.ttl.auth-code-minutes}") long authCodeTtl,
            @Value("${spring.data.redis.ttl.session-minutes}") long sessionTtl) {
        this.redisTemplate = redisTemplate;
        this.authCodeTtl = authCodeTtl;
        this.sessionTtl = sessionTtl;
    }

    // 이메일 인증 번호 저장 로직
    @Override
    public void saveAuthCode(String email, String code) {
        redisTemplate.opsForValue().set(
                "AUTH_CODE:" + email,
                code,
                Duration.ofMinutes(authCodeTtl)
        );
    }


    @Override
    public String getAndDeleteAuthCode(String email) {
        String key = "AUTH_CODE:" + email;
        return redisTemplate.opsForValue().getAndDelete(key);
    }

    @Override
    public boolean hasRecentRequest(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("LIMIT:" + email));
    }

    @Override
    public void saveSendLimit(String email, long limitSeconds) {
        // "LIMIT:이메일" 키를 만들고 지정된 시간(예: 60초) 뒤에 자동 삭제되게 함
        redisTemplate.opsForValue().set(
                "LIMIT:" + email,
                "SENT",
                Duration.ofSeconds(limitSeconds)
        );
    }

    // 회원가입 권한 세션 (30분) 정보 Redis DB 저장 로직
    @Override
    public void setRegisterSession(String email) {
        redisTemplate.opsForValue().set(
                "REGISTER_SESSION:" + email,
                "VALIDATED",
                Duration.ofMinutes(sessionTtl)
        );
    }

    @Override
    public boolean hasRegisterSession(String email) {
        return Boolean.TRUE.equals(redisTemplate.delete("REGISTER_SESSION:" + email));
    }

    @Override
    public void deleteRegisterSession(String email) {
        redisTemplate.delete("REGISTER_SESSION:" + email);
    }



}
