package com.example.anonymous_board.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.util.Date;

/**
 * JWT 블랙리스트 서비스
 * 로그아웃된 토큰을 Redis에 저장하여 무효화
 */
@Slf4j
@Service
public class JwtBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Key key;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    public JwtBlacklistService(
            RedisTemplate<String, Object> redisTemplate,
            @Value("${jwt.secret}") String secretKey) {
        this.redisTemplate = redisTemplate;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 토큰을 블랙리스트에 추가
     * TTL = 토큰의 남은 유효시간
     */
    public void blacklistToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            long remainingTime = expiration.getTime() - System.currentTimeMillis();

            if (remainingTime > 0) {
                String tokenKey = BLACKLIST_PREFIX + token;
                redisTemplate.opsForValue().set(tokenKey, "logout", Duration.ofMillis(remainingTime));
                log.info("JWT 블랙리스트에 추가됨: 남은 시간={}분", remainingTime / 60000);
            }
        } catch (Exception e) {
            log.warn("JWT 블랙리스트 추가 실패: {}", e.getMessage());
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     */
    public boolean isBlacklisted(String token) {
        String tokenKey = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey));
    }
}
