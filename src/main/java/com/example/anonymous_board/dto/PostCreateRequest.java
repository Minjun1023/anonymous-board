package com.example.anonymous_board.dto;

import lombok.Getter;

@Getter
public class PostCreateRequest {
    private String nickname;
    private String title;
    private String content;
    private String password;
}
