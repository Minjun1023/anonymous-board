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
public class AdminPostResponse {
    private Long id;
    private String title;
    private String nickname;
    private String content;
    private int viewCount;
    private LocalDateTime createdAt;
    private AuthorInfoResponse authorInfo;
}
