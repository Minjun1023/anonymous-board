package com.example.anonymous_board.dto;

import com.example.anonymous_board.domain.Message;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MessageDto {
    private Long id;
    private Long senderId;
    private String senderNickname;
    private Long receiverId;
    private String content;
    private LocalDateTime createdAt;

    public static MessageDto from(Message message) {
        MessageDto dto = new MessageDto();
        dto.id = message.getId();
        dto.senderId = message.getSender().getId();
        dto.senderNickname = message.getSender().getNickname();
        dto.receiverId = message.getReceiver().getId();
        dto.content = message.getContent();
        dto.createdAt = message.getCreatedAt();
        return dto;
    }
}
