package com.project.user;

import com.project.user.application.service.EmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class EmailIntegrationTest {

    @Autowired
    private EmailService emailService;

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private final String testEmail = "test1234@hufs.ac.kr";

    @BeforeEach
    void setUp() {
        // 테스트 시작 전 해당 이메일 관련 데이터가 있다면 삭제 (멱등성 확보)
        redisTemplate.delete("AUTH_CODE:" + testEmail);
    }

    @AfterEach
    void tearDown() {
        // 테스트가 끝난 후 사용한 데이터 즉시 삭제 (정리)
        redisTemplate.delete("AUTH_CODE:" + testEmail);
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

        System.out.println("발급된 인증번호: " + savedCode);
    }

    @Test
    @DisplayName("Redis에 저장된 인증번호의 유효 시간이 5분(300초)인지 검증한다")
    void checkAuthCodeTTL() {
        emailService.sendAuthEmail(testEmail);
        Long expireTime = redisTemplate.getExpire("AUTH_CODE:" + testEmail, TimeUnit.SECONDS);

        assertThat(expireTime).isNotNull();
        assertThat(expireTime).isGreaterThan(0);
        assertThat(expireTime).isLessThanOrEqualTo(300);
        assertThat(expireTime).isGreaterThan(295);
    }
}
