package com.example.anonymous_board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReadStatusDto {
    private Long senderId; // 발신자 ID (읽음 알림을 받을 사람)
    private Long receiverId; // 수신자 ID (메시지를 읽은 사람)
    private List<Long> messageIds; // 읽은 메시지 ID 목록
}
