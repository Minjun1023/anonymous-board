package com.example.anonymous_board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 사용자별 활성 토큰 관리 서비스
 * 한 계정당 하나의 토큰만 유지하도록 함
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActiveTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String ACTIVE_TOKEN_PREFIX = "active_token:";
    private static final Duration TOKEN_TTL = Duration.ofDays(1); // 24시간

    /**
     * 사용자의 활성 토큰 저장
     */
    public void saveActiveToken(String username, String token) {
        String key = ACTIVE_TOKEN_PREFIX + username;

        // 기존 토큰이 있으면 가져오기
        String oldToken = redisTemplate.opsForValue().get(key);
        log.info("ActiveTokenService: 사용자 {} 로그인 시도. 기존 토큰 존재 여부: {}", username, oldToken != null);

        // 새 토큰 저장
        redisTemplate.opsForValue().set(key, token, TOKEN_TTL);
        log.info("ActiveTokenService: 새 활성 토큰 저장 완료: username={}", username);

        // 기존 토큰이 있었다면 블랙리스트에 추가 (기존 세션 무효화)
        if (oldToken != null && !oldToken.equals(token)) {
            // JwtBlacklistService와 동일한 키 형식 사용
            redisTemplate.opsForValue().set("jwt:blacklist:" + oldToken, "invalidated", TOKEN_TTL);
            log.info("ActiveTokenService: 기존 토큰 블랙리스트 추가 완료: username={} (중복 로그인 방지)", username);
            if (oldToken.length() > 20) {
                log.info("ActiveTokenService: 무효화된 토큰: {}...", oldToken.substring(0, 20));
            } else {
                log.info("ActiveTokenService: 무효화된 토큰: {}", oldToken);
            }
        } else if (oldToken == null) {
            log.info("ActiveTokenService: 기존 토큰 없음 (첫 로그인)");
        } else {
            log.info("ActiveTokenService: 기존 토큰과 동일 (재로그인)");
        }
    }

    /**
     * 사용자의 활성 토큰 조회
     */
    public String getActiveToken(String username) {
        return redisTemplate.opsForValue().get(ACTIVE_TOKEN_PREFIX + username);
    }

    /**
     * 사용자의 활성 토큰 삭제 (로그아웃 시)
     */
    public void removeActiveToken(String username) {
        redisTemplate.delete(ACTIVE_TOKEN_PREFIX + username);
        log.info("활성 토큰 삭제: username={}", username);
    }

    /**
     * 토큰이 해당 사용자의 활성 토큰인지 확인
     */
    public boolean isActiveToken(String username, String token) {
        String activeToken = getActiveToken(username);
        return token.equals(activeToken);
    }
}
