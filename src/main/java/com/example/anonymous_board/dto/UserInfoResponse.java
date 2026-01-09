package com.example.anonymous_board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private Long userId; // 사용자 ID
    private String email; // 사용자 이메일
    private String name; // 사용자 이름
    private String provider; // 사용자 제공자
    private String role; // 사용자 역할
    private int postCount; // 게시글 수
    private int commentCount; // 댓글 수
    private LocalDateTime createdAt; // 생성 시간

    public static UserInfoResponse of(com.example.anonymous_board.domain.Member member) {
        return UserInfoResponse.builder()
                .userId(member.getId())
                .email(member.getEmail())
                .name(member.getUsername()) // Member에 name 필드가 없다면 username이나 nickname 사용
                .provider(member.getProvider())
                .role(member.getRole().name())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
