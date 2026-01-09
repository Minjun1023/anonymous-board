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
public class AuthorInfoResponse {
    private Long userId; // 사용자 ID
    private String email; // 사용자 이메일
    private String name; // 사용자 이름
    private String provider; // 사용자 제공자
    private String role; // 사용자 역할
    private LocalDateTime createdAt; // 사용자 생성 시간
}
