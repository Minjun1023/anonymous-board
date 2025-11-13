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
public class AdminCommentResponse {
    private Long id;
    private Long postId;
    private String postTitle;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;
    private AuthorInfoResponse authorInfo;
}
