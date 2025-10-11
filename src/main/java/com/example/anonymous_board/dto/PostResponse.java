package com.example.anonymous_board.dto;

import com.example.anonymous_board.entity.Post;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PostResponse {
    private final Long id;
    private final String nickname;
    private final String title;
    private final String content;
    private final List<CommentResponse> comments;

    public PostResponse(Post post) {
        this.id = post.getId();
        this.nickname = post.getNickname();
        this.title = post.getTitle();
        this.content = post.getContent();
        // Post 엔티티의 Comment 목록을 CommentResponse DTO 목록으로 변환
        this.comments = post.getComments().stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
    }
}
