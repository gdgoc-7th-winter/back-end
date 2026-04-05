package com.project.user.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.application.service.impl.EmailServiceImpl;
import com.project.user.domain.repository.UserRepository;
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class EmailServiceTest {

    @Autowired
    private EmailServiceImpl emailService;

    @MockitoBean
    private JavaMailSender mailSender;

    @MockitoBean
    private UserRepository userRepository;

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

    @Test
    @DisplayName("인증번호 불일치 시 재발급 제한(LIMIT 키)이 즉시 삭제된다")
    void wrongCodeDeletesSendLimit() {
        emailService.sendAuthEmail(testEmail);
        assertThat(redisTemplate.hasKey("LIMIT:" + testEmail)).isTrue();

        assertThatThrownBy(() -> emailService.verifyCode(testEmail, "000000"))
                .isInstanceOf(BusinessException.class);

        assertThat(redisTemplate.hasKey("LIMIT:" + testEmail)).isFalse();
    }

    @Test
    @DisplayName("이미 가입된 이메일로 인증 요청 시 DUPLICATED_ADDRESS 예외가 발생한다")
    void sendAuthEmail_alreadyRegistered_throwsDuplicatedAddress() {
        when(userRepository.existsByEmail(testEmail)).thenReturn(true);

        assertThatThrownBy(() -> emailService.sendAuthEmail(testEmail))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.DUPLICATED_ADDRESS));
    }

    @Test
    @DisplayName("인증번호 불일치 후 즉시 새 인증번호를 발급받을 수 있다")
    void wrongCodeAllowsImmediateResend() {
        emailService.sendAuthEmail(testEmail);

        assertThatThrownBy(() -> emailService.verifyCode(testEmail, "000000"))
                .isInstanceOf(BusinessException.class);

        // LIMIT 키가 없으므로 TOO_MANY_REQUESTS 없이 즉시 재발급 가능해야 함
        assertThatCode(() -> emailService.sendAuthEmail(testEmail))
                .doesNotThrowAnyException();
    }
}
