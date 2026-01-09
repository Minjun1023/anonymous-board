package com.example.anonymous_board.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * WebSocket 채팅 메시지 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class ChatMessageRequest {
    private Long receiverId; // 수신자 ID
    private String content; // 메시지 내용
}
