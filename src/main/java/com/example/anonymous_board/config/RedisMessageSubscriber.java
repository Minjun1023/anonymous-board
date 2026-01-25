package com.example.anonymous_board.config;

import com.example.anonymous_board.dto.MessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis Pub/Sub 메시지 구독자
 * Redis에서 메시지를 받아 WebSocket으로 전달
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    // Redis에서 메시지를 받아 WebSocket으로 전달
    @Override
    public void onMessage(@NonNull Message message, @Nullable byte[] pattern) {
        try {
            String rawMessage = new String(message.getBody());
            log.info("Redis 원본 메시지: {}", rawMessage);

            // GenericJackson2JsonRedisSerializer로 인해 이중 JSON 인코딩됨
            // 먼저 외부 JSON 레이어를 벗기고, 내부 JSON 문자열을 파싱
            String jsonString;
            if (rawMessage.startsWith("\"") && rawMessage.endsWith("\"")) {
                // 이중 인코딩된 경우: "\"...\""
                jsonString = objectMapper.readValue(rawMessage, String.class);
            } else {
                // 정상적인 JSON
                jsonString = rawMessage;
            }

            MessageDto chatMessage = objectMapper.readValue(jsonString, MessageDto.class);

            log.info("Redis 메시지 수신: senderId={}, receiverId={}",
                    chatMessage.getSenderId(), chatMessage.getReceiverId());

            // Topic 기반 메시지 전송 (더 안정적)
            // 수신자에게 전송: /topic/chat.user.{receiverId}
            String receiverTopic = "/topic/chat.user." + chatMessage.getReceiverId();
            messagingTemplate.convertAndSend(receiverTopic, chatMessage);
            log.info("수신자에게 전송: {}", receiverTopic);

            // 발신자에게도 전송 (본인이 보낸 메시지 확인용): /topic/chat.user.{senderId}
            String senderTopic = "/topic/chat.user." + chatMessage.getSenderId();
            messagingTemplate.convertAndSend(senderTopic, chatMessage);
            log.info("발신자에게 전송: {}", senderTopic);

            log.info("WebSocket 메시지 전송 완료");

        } catch (Exception e) {
            log.error("Redis 메시지 처리 실패: {}", e.getMessage(), e);
        }
    }
}
