package com.example.anonymous_board.service;

import com.example.anonymous_board.domain.Post;
import com.example.anonymous_board.repository.PostRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 핫 게시글 캐싱 서비스
 * Redis를 활용하여 인기 게시글 목록을 캐싱
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HotPostsCacheService {

    private final StringRedisTemplate redisTemplate;
    private final PostRepository postRepository;
    private final ObjectMapper objectMapper;

    // Redis 키
    private static final String HOT_POSTS_KEY = "cache:hot-posts";

    // 캐시 만료 시간 (10분)
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    /**
     * 핫 게시글 ID 목록 조회 (캐시 사용)
     * 캐시 미스 시 DB에서 조회 후 캐싱
     */
    public List<Long> getHotPostIds() {
        try {
            // 캐시에서 조회
            String cached = redisTemplate.opsForValue().get(HOT_POSTS_KEY);

            if (cached != null) {
                log.debug("핫 게시글 캐시 히트");
                return objectMapper.readValue(cached, new TypeReference<List<Long>>() {
                });
            }

            // 캐시 미스: DB에서 조회
            log.debug("핫 게시글 캐시 미스 - DB 조회");
            return refreshCache();

        } catch (JsonProcessingException e) {
            log.error("캐시 파싱 에러: {}", e.getMessage());
            return refreshCache();
        }
    }

    /**
     * 캐시 갱신 (DB에서 조회 후 Redis에 저장)
     */
    public List<Long> refreshCache() {
        // 상위 50개 핫 게시글 ID만 캐싱 (네트 스코어 10점 이상, 정렬은 Repository에서 처리)
        Pageable pageable = PageRequest.of(0, 50);
        Page<Post> hotPosts = postRepository.findHotPosts(10, pageable);

        List<Long> postIds = hotPosts.getContent().stream()
                .map(Post::getId)
                .collect(Collectors.toList());

        try {
            String json = objectMapper.writeValueAsString(postIds);
            redisTemplate.opsForValue().set(HOT_POSTS_KEY, json, CACHE_TTL);
            log.info("핫 게시글 캐시 갱신: {} 건, TTL=10분", postIds.size());
        } catch (JsonProcessingException e) {
            log.error("캐시 저장 에러: {}", e.getMessage());
        }

        return postIds;
    }

    /**
     * 캐시 무효화 (게시글 추천/비추천 시 호출)
     */
    public void invalidateCache() {
        redisTemplate.delete(HOT_POSTS_KEY);
        log.debug("핫 게시글 캐시 무효화");
    }

    /**
     * 캐시 존재 여부 확인
     */
    public boolean isCached() {
        return Boolean.TRUE.equals(redisTemplate.hasKey(HOT_POSTS_KEY));
    }
}
