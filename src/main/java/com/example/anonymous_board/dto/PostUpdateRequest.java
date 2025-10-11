package com.example.anonymous_board.dto;

import lombok.Getter;

@Getter
public class PostUpdateRequest {
    private String nickname;
    private String title;
    private String content;
    private String password;
}
