package com.example.anonymous_board.controllers.websocket;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.dto.ChatMessageRequest;
import com.example.anonymous_board.dto.MessageDto;
import com.example.anonymous_board.dto.MessageCreateRequest;
import com.example.anonymous_board.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket 채팅 컨트롤러
 * STOMP 프로토콜을 통한 실시간 메시지 처리
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final MessageService messageService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CHAT_CHANNEL = "chat";

    /**
     * 채팅 메시지 전송
     * 클라이언트에서 /app/chat.send 로 메시지를 보내면 처리
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        try {
            log.info("sendMessage 호출됨 - principal: {}", principal);

            if (principal == null) {
                log.warn("인증되지 않은 사용자의 메시지 전송 시도");
                return;
            }

            // Principal에서 Member 추출
            Member sender = extractMember(principal);
            if (sender == null) {
                log.warn("사용자 정보를 찾을 수 없습니다.");
                return;
            }

            log.info("메시지 전송: senderId={}, receiverId={}, content={}",
                    sender.getId(), request.getReceiverId(), request.getContent());

            // MessageCreateRequest로 변환
            MessageCreateRequest createRequest = new MessageCreateRequest();
            createRequest.setReceiverId(request.getReceiverId());
            createRequest.setContent(request.getContent());

            // 메시지 저장 (DB에 영구 저장)
            MessageDto savedMessage = messageService.sendMessage(sender.getId(), createRequest);

            // Redis Pub/Sub로 메시지 브로드캐스트
            String messageJson = objectMapper.writeValueAsString(savedMessage);
            redisTemplate.convertAndSend(CHAT_CHANNEL, messageJson);

            log.info("Redis로 메시지 발행 완료: messageId={}", savedMessage.getId());

        } catch (Exception e) {
            log.error("메시지 전송 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 타이핑 상태 전송 (선택 기능)
     * 클라이언트에서 /app/chat.typing 으로 보내면 처리
     */
    @MessageMapping("/chat.typing")
    public void typing(@Payload ChatMessageRequest request, Principal principal) {
        try {
            if (principal == null)
                return;

            Member sender = extractMember(principal);
            if (sender == null)
                return;

            // 상대방에게 타이핑 상태 알림
            // 이 부분은 Redis 없이 직접 WebSocket으로 전송할 수도 있음
            log.debug("타이핑 상태: senderId={}, receiverId={}", sender.getId(), request.getReceiverId());

        } catch (Exception e) {
            log.error("타이핑 상태 전송 실패: {}", e.getMessage());
        }
    }

    private Member extractMember(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken) {
            Object principalObj = ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
            if (principalObj instanceof Member) {
                return (Member) principalObj;
            }
        }
        return null;
    }
}
