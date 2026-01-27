package com.example.anonymous_board.dto;

import com.example.anonymous_board.domain.Message;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MessageDto {
    private Long id; // 메시지 ID
    private Long senderId; // 발신자 ID
    private String senderNickname; // 발신자 닉네임
    private String senderProfileImage; // 발신자 프로필 이미지
    private Long receiverId; // 수신자 ID
    private String content; // 메시지 내용
    private LocalDateTime createdAt; // 생성 시간
    private LocalDateTime readAt; // 읽음 시간

    public static MessageDto from(Message message) {
        MessageDto dto = new MessageDto();
        dto.id = message.getId();
        dto.senderId = message.getSender().getId();
        dto.senderNickname = message.getSender().getNickname();

        // 프로필 이미지 처리
        String profileImage = message.getSender().getProfileImage();
        if (profileImage != null && !profileImage.startsWith("/profiles/") && !profileImage.startsWith("http")) {
            profileImage = "/profiles/" + profileImage;
        } else if (profileImage == null) {
            profileImage = "/profiles/default_profile.png";
        }
        dto.senderProfileImage = profileImage;

        dto.receiverId = message.getReceiver().getId();
        dto.content = message.getContent();
        dto.createdAt = message.getCreatedAt();
        dto.readAt = message.getReadAt(); // 읽음 시간 추가
        return dto;
    }
}
