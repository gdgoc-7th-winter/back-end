package com.project.user.application.service;

import com.project.user.application.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class EmailServiceTest {

    @MockitoBean
    private software.amazon.awssdk.services.s3.S3Client s3Client;

    @Autowired
    private EmailServiceImpl emailService;

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final String testEmail = "test" + System.currentTimeMillis() + "@hufs.ac.kr";

    @BeforeEach
    void setUp() {
        redisTemplate.delete("AUTH_CODE:" + testEmail);
        redisTemplate.delete("LIMIT:" + testEmail);
    }

    @AfterEach
    void tearDown() {
        redisTemplate.delete("AUTH_CODE:" + testEmail);
        redisTemplate.delete("LIMIT:" + testEmail);
    }

    @Test
    @DisplayName("실제 로직을 실행하여 Redis에 6자리 난수가 저장되는지 확인한다")
    void sendEmailAndCheckRedis() {
        // 1. 발송 로직 실행 (실제 메일 발송 포함)
        emailService.sendAuthEmail(testEmail);

        // 2. Redis에서 직접 데이터 꺼내기
        String savedCode = redisTemplate.opsForValue().get("AUTH_CODE:" + testEmail);

        // 3. 검증
        assertThat(savedCode).isNotNull();
        assertThat(savedCode).hasSize(6);
        assertThat(savedCode).containsOnlyDigits();
    }

    @Test
    @DisplayName("Redis 키와 값을 직접 출력하며 확인")
    void checkAuthCodeTTL() {
        emailService.sendAuthEmail(testEmail);
        String targetKey = "AUTH_CODE:" + testEmail;
        Long expireTime = redisTemplate.getExpire(targetKey, TimeUnit.SECONDS);
        assertThat(expireTime).isGreaterThan(290L);
    }
}
