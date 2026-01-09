package com.example.anonymous_board.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyCommentResponse {
    private Long id; // 댓글 ID
    private Long postId; // 게시글 ID
    private String postTitle; // 게시글 제목
    private String content; // 댓글 내용
    private LocalDateTime createdAt; // 댓글 생성 시간
}
