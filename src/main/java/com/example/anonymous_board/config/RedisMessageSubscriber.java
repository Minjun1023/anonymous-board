package com.example.anonymous_board.config;

import com.example.anonymous_board.dto.MessageDto;
import com.example.anonymous_board.dto.ReadStatusDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
            String jsonString;
            if (rawMessage.startsWith("\"") && rawMessage.endsWith("\"")) {
                jsonString = objectMapper.readValue(rawMessage, String.class);
            } else {
                jsonString = rawMessage;
            }

            // JSON을 먼저 JsonNode로 파싱하여 타입 확인
            JsonNode node = objectMapper.readTree(jsonString);

            if (node.has("messageIds")) {
                // ReadStatusDto인 경우 - 읽음 상태 업데이트
                // GenericJackson2JsonRedisSerializer로 인한 타입 정보 제거
                ReadStatusDto readStatus = new ReadStatusDto();
                readStatus.setSenderId(node.get("senderId").asLong());
                readStatus.setReceiverId(node.get("receiverId").asLong());

                // messageIds 배열 수동 파싱 (타입 정보 무시)
                List<Long> messageIds = new ArrayList<>();
                JsonNode messageIdsNode = node.get("messageIds");
                if (messageIdsNode.isArray()) {
                    for (JsonNode idNode : messageIdsNode) {
                        // 타입 정보 무시하고 숫자만 추출
                        if (idNode.isNumber()) {
                            messageIds.add(idNode.asLong());
                        } else if (idNode.isArray()) {
                            // ["java.util.ArrayList", [115]] 형식 처리
                            for (JsonNode innerNode : idNode) {
                                if (innerNode.isNumber()) {
                                    messageIds.add(innerNode.asLong());
                                }
                            }
                        }
                    }
                }
                readStatus.setMessageIds(messageIds);

                log.info("읽음 상태 수신: senderId={}, messageIds={}",
                        readStatus.getSenderId(), readStatus.getMessageIds());

                // 발신자에게만 전송 (메시지를 보낸 사람이 읽음 상태를 확인)
                String senderTopic = "/topic/chat.user." + readStatus.getSenderId();
                messagingTemplate.convertAndSend(senderTopic, readStatus);
                log.info("읽음 상태 전송: {}", senderTopic);

            } else {
                // MessageDto인 경우 - 일반 채팅 메시지
                MessageDto chatMessage = objectMapper.readValue(jsonString, MessageDto.class);

                log.info("채팅 메시지 수신: senderId={}, receiverId={}",
                        chatMessage.getSenderId(), chatMessage.getReceiverId());

                // 수신자에게 전송
                String receiverTopic = "/topic/chat.user." + chatMessage.getReceiverId();
                messagingTemplate.convertAndSend(receiverTopic, chatMessage);
                log.info("수신자에게 전송: {}", receiverTopic);

                // 발신자에게도 전송 (본인이 보낸 메시지 확인용)
                String senderTopic = "/topic/chat.user." + chatMessage.getSenderId();
                messagingTemplate.convertAndSend(senderTopic, chatMessage);
                log.info("발신자에게 전송: {}", senderTopic);
            }

            log.info("WebSocket 메시지 전송 완료");

        } catch (Exception e) {
            log.error("Redis 메시지 처리 실패: {}", e.getMessage(), e);
        }
    }
}
