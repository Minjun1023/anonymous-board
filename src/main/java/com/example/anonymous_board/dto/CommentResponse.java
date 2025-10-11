package com.example.anonymous_board.dto;

import com.example.anonymous_board.domain.Comment;
import lombok.Getter;

@Getter
public class CommentResponse {
    private final Long id;
    private final String nickname;
    private final String content;

    public CommentResponse(Comment comment) {
        this.id = comment.getId();
        this.nickname = comment.getNickname();
        this.content = comment.getContent();
    }
}
