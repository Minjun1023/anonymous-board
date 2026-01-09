package com.example.anonymous_board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageCreateRequest {
    private Long receiverId; // 수신자 ID
    private String content; // 메시지 내용
}
