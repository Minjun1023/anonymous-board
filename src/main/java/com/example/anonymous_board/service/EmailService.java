package com.example.anonymous_board.service;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.dto.EmailCheckRequest;
import com.example.anonymous_board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

/**
 * 이메일 인증 서비스
 * Redis를 활용한 TTL 기반 토큰 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final RedisEmailTokenService redisEmailTokenService;

    /**
     * 회원가입 이메일 인증 코드 발송
     */
    public void sendVerificationEmail(String email) {
        // 이미 가입된 이메일인지 확인
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 발송 속도 제한 확인 (1분에 1회)
        if (redisEmailTokenService.isRateLimited(email)) {
            throw new IllegalArgumentException("1분 이내에 이메일을 보낼 수 없습니다. 잠시 후 다시 시도해주세요.");
        }

        // 새로운 6자리 숫자 토큰 생성
        String tokenValue = String.format("%06d", new Random().nextInt(999999));

        // Redis에 토큰 저장 (5분 TTL)
        redisEmailTokenService.saveVerificationToken(email, tokenValue);

        // 발송 속도 제한 설정
        redisEmailTokenService.setRateLimit(email);

        // 이메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[익명게시판] 회원가입 이메일 인증 코드");
        message.setText("인증 코드는 다음과 같습니다: " + tokenValue + "\n\n이 코드는 5분 후에 만료됩니다.");

        try {
            mailSender.send(message);
            log.info("이메일 인증 코드 발송 완료: email={}", email);
        } catch (Exception e) {
            log.error("이메일 발송 실패: email={}, error={}", email, e.getMessage());
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }

    /**
     * 회원가입 이메일 인증 코드 확인
     */
    public void verifyEmail(EmailCheckRequest request) {
        String storedToken = redisEmailTokenService.getVerificationToken(request.getEmail());

        if (storedToken == null) {
            throw new IllegalArgumentException("유효한 인증 토큰을 찾을 수 없습니다. 인증 코드가 만료되었을 수 있습니다.");
        }

        // 토큰 값 비교
        if (!storedToken.equals(request.getToken())) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }

        // 인증 완료 상태 저장 (Redis에 30분간 유지)
        redisEmailTokenService.setEmailVerified(request.getEmail());
        log.info("이메일 인증 완료: email={}", request.getEmail());
    }

    /**
     * 이메일 인증 완료 여부 확인
     */
    public boolean isEmailVerified(String email) {
        return redisEmailTokenService.isEmailVerified(email);
    }

    /**
     * 비밀번호 재설정 이메일 발송
     */
    public void sendPasswordResetLink(Member member) {
        String email = member.getEmail();

        // 발송 속도 제한 확인
        if (redisEmailTokenService.isRateLimited(email)) {
            throw new IllegalArgumentException("1분 이내에 이메일을 보낼 수 없습니다. 잠시 후 다시 시도해주세요.");
        }

        // UUID 토큰 생성
        String tokenValue = UUID.randomUUID().toString();

        // Redis에 토큰 저장 (5분 TTL)
        redisEmailTokenService.savePasswordResetToken(email, tokenValue);

        // 발송 속도 제한 설정
        redisEmailTokenService.setRateLimit(email);

        String resetLink = "http://localhost:8080/new-password?token=" + tokenValue;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[익명게시판] 비밀번호 재설정 링크");
        message.setText("비밀번호를 재설정하려면 다음 링크를 클릭하세요: " + resetLink + "\n\n이 링크는 5분 후에 만료됩니다.");

        try {
            mailSender.send(message);
            log.info("비밀번호 재설정 이메일 발송 완료: email={}", email);
        } catch (Exception e) {
            log.error("이메일 발송 실패: email={}, error={}", email, e.getMessage());
            throw new RuntimeException("이메일 발송에 실패했습니다.");
        }
    }
}
