package com.example.anonymous_board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentCreateRequest {
    private String content;
    private boolean secret;
}
