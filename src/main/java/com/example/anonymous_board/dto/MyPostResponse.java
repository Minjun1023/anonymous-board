package com.example.anonymous_board.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyPostResponse {
    private Long id; // 게시글 ID
    private String title; // 게시글 제목
    private String content; // 게시글 내용
    private int viewCount; // 조회수
    private int commentCount; // 댓글 수
    private LocalDateTime createdAt; // 생성 시간
}
