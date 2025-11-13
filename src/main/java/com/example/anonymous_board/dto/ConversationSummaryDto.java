package com.example.anonymous_board.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ConversationSummaryDto {
    private Long otherUserId;
    private String otherUserNickname;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    private boolean hasUnreadMessages; // Optional: to indicate new messages
}
