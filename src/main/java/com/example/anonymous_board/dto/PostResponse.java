package com.example.anonymous_board.dto;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Post;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PostResponse {
    private final Long id;
    private final Long authorId;
    private final String nickname;
    private final String title;
    private final String content;
    private final LocalDateTime createdAt;
    private final int viewCount;
    private final int likes;
    private final int dislikes;
    private final int commentCount;
    @JsonProperty("isOwner")
    private final boolean isOwner;
    private final List<CommentResponse> comments;

    public PostResponse(Post post, Member currentUser) {
        this.id = post.getId();
        this.authorId = post.getMember().getId();
        this.nickname = post.getNickname();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt();
        this.viewCount = post.getViewCount();
        this.likes = post.getLikes();
        this.dislikes = post.getDislikes();
        this.commentCount = post.getCommentCount();
        this.isOwner = (currentUser != null && post.getMember().getId().equals(currentUser.getId()));
        this.comments = post.getComments().stream()
                .map(comment -> new CommentResponse(comment, currentUser))
                .collect(Collectors.toList());
    }
}
