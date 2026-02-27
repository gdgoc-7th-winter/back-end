package com.project.user.application.service;

import com.project.user.domain.repository.EmailAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final EmailAuthRepository emailAuthRepository;

    // 이메일 도메인 유효성 검사
    public void validateHufsEmail(String email) {
        if (!email.endsWith("@hufs.ac.kr")) {
            throw new IllegalArgumentException("한국외대 이메일(@hufs.ac.kr)만 사용 가능합니다.");
        }
    }

    // 인증코드 (6자리 난수) 생성
    private String generateAuthCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    // 이메일 발송
    public void sendAuthEmail(String email) {
        validateHufsEmail(email);
        String authCode = generateAuthCode();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[HUFS Community] 회원가입 인증번호");
        message.setText("인증번호는 [" + authCode + "] 입니다. 5분 이내에 입력해주세요.");

        try {
            mailSender.send(message); // 실제 발송
            emailAuthRepository.saveAuthCode(email, authCode);
        } catch (Exception e) {
            throw new RuntimeException("메일 발송 중 오류가 발생했습니다.");
        }
    }

    public String getAuthInfo(String email) {
        return emailAuthRepository.getAuthCode(email);
    }

    // 인증코드 확인 및 이메일 인증 정보 Redis DB 인스턴스 삭제
    public void verifyCode(String email, String inputCode) {
        String savedCode = emailAuthRepository.getAuthCode(email);

        if (savedCode == null || !savedCode.equals(inputCode)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않거나 만료되었습니다.");
        }

        emailAuthRepository.deleteAuthcode(email);
        emailAuthRepository.setRegisterSession(email);
    }
}
