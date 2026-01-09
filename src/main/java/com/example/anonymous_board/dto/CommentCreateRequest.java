package com.example.anonymous_board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentCreateRequest {
    private String content; // 댓글 내용
    private boolean secret; // 비밀 댓글 여부
    private Long parentId; // 부모 댓글 ID (대댓글일 경우)
}
