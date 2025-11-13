package com.example.anonymous_board.service;

import com.example.anonymous_board.domain.EmailVerificationToken;
import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.TokenType;
import com.example.anonymous_board.dto.EmailCheckRequest;
import com.example.anonymous_board.repository.EmailVerificationTokenRepository;
import com.example.anonymous_board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public void sendVerificationEmail(String email) {
        // 이미 가입된 이메일인지 확인
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 5분 이내에 발송된 토큰이 있는지 확인
        tokenRepository.findByEmailAndTokenType(email, TokenType.EMAIL_VERIFICATION).ifPresent(token -> {
            if (token.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(5))) {
                throw new IllegalArgumentException("5분 이내에 이메일을 보낼 수 없습니다.");
            }
            tokenRepository.delete(token);
            tokenRepository.flush(); // 변경사항을 즉시 DB에 반영
        });

        // 새로운 6자리 숫자 토큰 생성
        String tokenValue = String.format("%06d", new Random().nextInt(999999));
        EmailVerificationToken verificationToken = EmailVerificationToken.create(email, tokenValue, TokenType.EMAIL_VERIFICATION);
        tokenRepository.save(verificationToken);

        // 이메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[익명게시판] 회원가입 이메일 인증 코드");
        message.setText("인증 코드는 다음과 같습니다: " + tokenValue);
        
        try {
            mailSender.send(message);
            log.info("Verification email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", email, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.");
        }
    }

    @Transactional
    public void verifyEmail(EmailCheckRequest request) {
        // 가장 최근의 유효한 토큰을 찾음
        EmailVerificationToken token = tokenRepository.findByEmailAndTokenType(request.getEmail(), TokenType.EMAIL_VERIFICATION)
                .filter(t -> !t.isExpired() && !t.isVerified())
                .orElseThrow(() -> new IllegalArgumentException("유효한 인증 토큰을 찾을 수 없습니다."));

        // 토큰 값 비교
        if (!token.getToken().equals(request.getToken())) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }

        // 시간 만료 확인
        if (token.getExpirationTime().isBefore(LocalDateTime.now())) {
            token.setExpired();
            throw new IllegalArgumentException("인증 코드가 만료되었습니다.");
        }

        // 인증 완료 처리
        token.setVerified();
    }

    @Transactional
    public void sendPasswordResetLink(Member member) {
        // 5분 이내에 발송된 토큰이 있는지 확인
        tokenRepository.findByEmailAndTokenType(member.getEmail(), TokenType.PASSWORD_RESET).ifPresent(token -> {
            if (!token.isExpired() && token.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(5))) {
                throw new IllegalArgumentException("5분 이내에 이메일을 보낼 수 없습니다.");
            }
            tokenRepository.delete(token);
            tokenRepository.flush(); // 변경사항을 즉시 DB에 반영
        });

        String tokenValue = UUID.randomUUID().toString();
        EmailVerificationToken passwordResetToken = EmailVerificationToken.create(member.getEmail(), tokenValue, TokenType.PASSWORD_RESET);
        tokenRepository.save(passwordResetToken);

        String resetLink = "http://localhost:8080/new-password?token=" + tokenValue;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(member.getEmail());
        message.setSubject("[익명게시판] 비밀번호 재설정 링크");
        message.setText("비밀번호를 재설정하려면 다음 링크를 클릭하세요: " + resetLink);

        try {
            mailSender.send(message);
            log.info("Password reset link sent to: {}", member.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email to: {}", member.getEmail(), e);
            throw new RuntimeException("이메일 발송에 실패했습니다.");
        }
    }
}
