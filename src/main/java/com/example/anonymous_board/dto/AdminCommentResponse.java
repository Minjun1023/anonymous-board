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
    private Long id; // 댓글 ID
    private Long postId; // 게시글 ID
    private String postTitle; // 게시글 제목
    private String nickname; // 댓글 작성자 닉네임
    private String content; // 댓글 내용
    private LocalDateTime createdAt; // 댓글 생성 시간
    private AuthorInfoResponse authorInfo; // 댓글 작성자 정보
}
