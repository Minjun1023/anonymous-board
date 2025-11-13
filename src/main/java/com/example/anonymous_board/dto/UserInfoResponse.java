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
    private Long userId;
    private String email;
    private String name;
    private String provider;
    private String role;
    private int postCount;
    private int commentCount;
    private LocalDateTime createdAt;
}
