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
    private Long id; // 게시글 ID
    private String title; // 게시글 제목
    private String nickname; // 게시글 작성자 닉네임
    private String content; // 게시글 내용
    private int viewCount; // 게시글 조회수
    private LocalDateTime createdAt; // 게시글 생성 시간
    private AuthorInfoResponse authorInfo; // 게시글 작성자 정보
}
