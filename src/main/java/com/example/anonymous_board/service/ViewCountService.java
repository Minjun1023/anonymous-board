package com.example.anonymous_board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis 기반 조회수 관리 서비스
 * IP/사용자별 중복 조회 방지
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ViewCountService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Redis 키 prefix
    private static final String VIEW_RECORD_PREFIX = "view:post:";

    // 중복 조회 방지 시간 (24시간)
    private static final Duration VIEW_EXPIRATION = Duration.ofHours(24);

    /**
     * 조회 가능 여부 확인 및 기록
     * 같은 게시글을 24시간 내 같은 IP/사용자가 조회했으면 false 반환
     * 
     * @param postId           게시글 ID
     * @param viewerIdentifier IP 주소 또는 사용자 ID
     * @return true면 조회수 증가 가능, false면 이미 조회함
     */
    public boolean canIncrementViewCount(Long postId, String viewerIdentifier) {
        String key = VIEW_RECORD_PREFIX + postId + ":" + viewerIdentifier;

        // 이미 조회한 기록이 있는지 확인
        Boolean exists = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(exists)) {
            log.debug("중복 조회 방지: postId={}, viewer={}", postId, viewerIdentifier);
            return false;
        }

        // 조회 기록 저장 (24시간 후 자동 삭제)
        redisTemplate.opsForValue().set(key, "1", VIEW_EXPIRATION);
        log.debug("조회 기록 저장: postId={}, viewer={}, ttl=24h", postId, viewerIdentifier);

        return true;
    }

    /**
     * 특정 게시글의 조회 기록 삭제 (테스트/관리용)
     */
    public void clearViewRecord(Long postId, String viewerIdentifier) {
        String key = VIEW_RECORD_PREFIX + postId + ":" + viewerIdentifier;
        redisTemplate.delete(key);
    }
}
