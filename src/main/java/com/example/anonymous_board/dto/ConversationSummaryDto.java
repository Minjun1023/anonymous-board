package com.example.anonymous_board.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ConversationSummaryDto {
    private Long otherUserId; // 상대방 사용자 ID
    private String otherUserNickname; // 상대방 사용자 닉네임
    private String profileImage; // 상대방 사용자 프로필 이미지
    private String lastMessageContent; // 마지막 메시지 내용
    private LocalDateTime lastMessageTime; // 마지막 메시지 시간
    private boolean hasUnreadMessages; // 읽지 않은 메시지 여부
}
