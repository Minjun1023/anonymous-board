package com.example.anonymous_board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentUpdateRequest {
    private String content; // 댓글 내용
}
