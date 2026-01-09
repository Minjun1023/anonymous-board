package com.example.anonymous_board.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

/**
 * WebSocket 설정
 * STOMP 프로토콜을 사용한 실시간 채팅 지원
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 구독할 주제 prefix
        // /topic: 1:N 브로드캐스트
        // /queue: 1:1 개인 메시지
        config.enableSimpleBroker("/topic", "/queue");

        // 클라이언트가 메시지를 보낼 때 사용할 prefix
        config.setApplicationDestinationPrefixes("/app");

        // 특정 사용자에게 메시지 보낼 때 prefix
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // WebSocket 인증 인터셉터 등록
        registration.interceptors(webSocketAuthInterceptor);
    }
}
