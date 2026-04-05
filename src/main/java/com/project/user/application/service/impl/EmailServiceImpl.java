package com.project.user.application.service.impl;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.domain.repository.EmailAuthRepository;
import com.project.user.domain.repository.UserRepository;
import com.project.user.domain.repository.impl.RedisEmailAuthRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Slf4j
@Service
public class EmailServiceImpl implements com.project.user.application.service.EmailService {
    private final JavaMailSender mailSender;
    private final EmailAuthRepository emailAuthRepository;
    private final UserRepository userRepository;
    private final long authCodeTtl;
    private final long limitSeconds;

    public EmailServiceImpl(
            JavaMailSender mailSender,
            RedisEmailAuthRepository emailAuthRepository,
            UserRepository userRepository,
            @Value("${spring.data.redis.ttl.auth-code-minutes:5}") long authCodeTtl,
            @Value("${spring.data.redis.ttl.limit-seconds:60}") long limitSeconds) {
        this.mailSender = mailSender;
        this.emailAuthRepository = emailAuthRepository;
        this.userRepository = userRepository;
        this.authCodeTtl = authCodeTtl;
        this.limitSeconds = limitSeconds;
    }

    // 이메일 도메인 유효성 검사 (hufs.ac.kr)
    @Value("${spring.mail.domain}")
    private String emailDomain;

    @Override
    public void validateHufsEmail(String email) {
        if (!email.endsWith(emailDomain)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,"한국외대 이메일(@hufs.ac.kr)만 사용 가능합니다.");
        }
    }

    // 이메일 발송
    @Override
    public void sendAuthEmail(String email) {
        validateHufsEmail(email);

        // 이미 가입된 이메일 예외처리
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATED_ADDRESS, "이미 가입된 이메일 주소입니다.");
        }

        // 중복요청 예외처리
        if (emailAuthRepository.hasRecentRequest(email)) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
        }

        // 이메일 발송 로직 1: 인증코드 및 메일 내용 형성
        String authCode = generateAuthCode();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[HUFS Community] 회원가입 인증번호");
        message.setText("인증번호는 [" + authCode + "] 입니다. [" + authCodeTtl + "]분 이내에 입력해주세요.");

        // 이메일 발송 로직 2: 인증번호 정보 Redis DB 저장 (TTL 5분)
        try {
            emailAuthRepository.saveAuthCode(email, authCode);
            mailSender.send(message); // 실제 발송
            emailAuthRepository.saveSendLimit(email, limitSeconds);
        } catch (Exception e) {
            log.error("메일 발송 실패: email={}", email, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이메일 서버에 일시적인 오류가 발생했습니다. 죄송합니다.");
        }
    }

    // 인증코드 확인 및 이메일 인증 정보 Redis DB 인스턴스 삭제
    @Override
    public void verifyCode(String email, String authCode) {
        String savedCode = emailAuthRepository.getAndDeleteAuthCode(email);

        if (savedCode == null) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND); // 세션이 만료되거나 존재하지 않음
        }

        if (!savedCode.equals(authCode)) {
            // 인증번호가 틀린 경우 재발급 제한도 즉시 해제하여 새 코드를 바로 요청할 수 있게 함
            emailAuthRepository.deleteSendLimit(email);
            throw new BusinessException(ErrorCode.INVALID_AUTH_CODE); // 번호 틀림
        }
        // 회원가입 권한 세션 저장
        emailAuthRepository.setRegisterSession(email);
    }

    // 인증코드 (6자리 난수) 생성
    private String generateAuthCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
