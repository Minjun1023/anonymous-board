package com.example.anonymous_board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis 기반 이메일 인증 토큰 관리 서비스
 * TTL을 활용하여 자동 만료 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisEmailTokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Redis 키 prefix
    private static final String EMAIL_VERIFICATION_PREFIX = "email:verification:";
    private static final String PASSWORD_RESET_PREFIX = "email:password-reset:";
    private static final String EMAIL_VERIFIED_PREFIX = "email:verified:";
    private static final String RATE_LIMIT_PREFIX = "email:rate-limit:";

    // TTL 설정
    private static final Duration TOKEN_TTL = Duration.ofMinutes(5); // 토큰 유효기간: 5분
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(30); // 인증 완료 상태 유지: 30분
    private static final Duration RATE_LIMIT_TTL = Duration.ofMinutes(1); // 재발송 제한: 1분

    /**
     * 이메일 인증 코드 저장
     */
    public void saveVerificationToken(String email, String token) {
        String key = EMAIL_VERIFICATION_PREFIX + email;
        redisTemplate.opsForValue().set(key, token, TOKEN_TTL);
        log.info("이메일 인증 코드 저장: email={}, ttl={}분", email, TOKEN_TTL.toMinutes());
    }

    /**
     * 이메일 인증 코드 조회
     */
    public String getVerificationToken(String email) {
        String key = EMAIL_VERIFICATION_PREFIX + email;
        Object token = redisTemplate.opsForValue().get(key);
        return token != null ? token.toString() : null;
    }

    /**
     * 이메일 인증 코드 삭제
     */
    public void deleteVerificationToken(String email) {
        String key = EMAIL_VERIFICATION_PREFIX + email;
        redisTemplate.delete(key);
    }

    /**
     * 이메일 인증 완료 상태 저장
     */
    public void setEmailVerified(String email) {
        String key = EMAIL_VERIFIED_PREFIX + email;
        redisTemplate.opsForValue().set(key, "true", VERIFIED_TTL);
        // 인증 코드 삭제
        deleteVerificationToken(email);
        log.info("이메일 인증 완료: email={}", email);
    }

    /**
     * 이메일 인증 완료 여부 확인
     */
    public boolean isEmailVerified(String email) {
        String key = EMAIL_VERIFIED_PREFIX + email;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 비밀번호 재설정 토큰 저장
     */
    public void savePasswordResetToken(String email, String token) {
        String key = PASSWORD_RESET_PREFIX + token; // 토큰으로 조회하므로 토큰을 키로 사용
        redisTemplate.opsForValue().set(key, email, TOKEN_TTL);
        log.info("비밀번호 재설정 토큰 저장: email={}", email);
    }

    /**
     * 비밀번호 재설정 토큰으로 이메일 조회
     */
    public String getEmailByPasswordResetToken(String token) {
        String key = PASSWORD_RESET_PREFIX + token;
        Object email = redisTemplate.opsForValue().get(key);
        return email != null ? email.toString() : null;
    }

    /**
     * 비밀번호 재설정 토큰 삭제
     */
    public void deletePasswordResetToken(String token) {
        String key = PASSWORD_RESET_PREFIX + token;
        redisTemplate.delete(key);
    }

    /**
     * 이메일 발송 속도 제한 확인 (1분에 1회)
     */
    public boolean isRateLimited(String email) {
        String key = RATE_LIMIT_PREFIX + email;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 이메일 발송 속도 제한 설정
     */
    public void setRateLimit(String email) {
        String key = RATE_LIMIT_PREFIX + email;
        redisTemplate.opsForValue().set(key, "1", RATE_LIMIT_TTL);
    }
}
