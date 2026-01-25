package com.example.anonymous_board.config;

import com.example.anonymous_board.config.jwt.JwtTokenProvider;
import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * WebSocket 연결 시 JWT 인증 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
// ChannelInterceptor 인터페이스 구현
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    // WebSocket 연결 전 인증 처리
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // STOMP 헤더에서 인증 정보 추출
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // STOMP CONNECT 시 인증 처리
            String token = extractToken(accessor);

            // 토큰 검증
            if (token != null && jwtTokenProvider.validateToken(token)) {
                try {
                    // 이메일로 사용자 조회
                    String email = jwtTokenProvider.getEmail(token);
                    Member member = userRepository.findByEmail(email).orElse(null);

                    if (member != null) {
                        // 인증 객체 생성
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                member,
                                null,
                                Collections
                                        .singletonList(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())));

                        // 인증 정보 설정
                        accessor.setUser(auth);
                        log.debug("WebSocket 인증 성공: {}", member.getNickname());
                    }
                } catch (Exception e) {
                    log.error("WebSocket 인증 실패: {}", e.getMessage());
                }
            }
        }

        return message;
    }

    private String extractToken(StompHeaderAccessor accessor) {
        // 1. Authorization 헤더에서 토큰 추출
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2. 쿠키에서 토큰 추출 (access_token)
        String cookieHeader = accessor.getFirstNativeHeader("Cookie");
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                String trimmed = cookie.trim();
                if (trimmed.startsWith("access_token=")) {
                    return trimmed.substring("access_token=".length());
                }
            }
        }

        // 3. 커스텀 헤더에서 토큰 추출
        String tokenHeader = accessor.getFirstNativeHeader("token");
        if (tokenHeader != null) {
            return tokenHeader;
        }

        return null;
    }
}
