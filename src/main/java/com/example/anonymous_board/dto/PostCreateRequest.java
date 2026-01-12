package com.example.anonymous_board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class PostCreateRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    private String title; // 게시글 제목

    @NotBlank(message = "내용을 입력해주세요.")
    private String content; // 게시글 내용

    private String pollQuestion; // 투표 질문
    private java.util.List<String> pollOptions; // 투표 옵션 리스트
    private String password; // 게시글 비밀번호
    private Boolean isAnnouncement = false; // 공지사항 여부 (관리자 전용)
}
