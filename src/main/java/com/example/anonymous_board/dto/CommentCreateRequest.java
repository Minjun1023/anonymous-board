package com.example.anonymous_board.dto;

import lombok.Getter;

@Getter
public class CommentCreateRequest {
    private String nickname;
    private String content;
    private String password;
}
