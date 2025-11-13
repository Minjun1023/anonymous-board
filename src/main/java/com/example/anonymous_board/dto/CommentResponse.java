package com.example.anonymous_board.dto;

import com.example.anonymous_board.domain.Comment;
import com.example.anonymous_board.domain.Member;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CommentResponse {
    private final Long id;
    private final String nickname;
    private final String content;
    private final boolean secret;
    @JsonProperty("isOwner")
    private final boolean isOwner;

    public CommentResponse(Comment comment, Member currentUser) {
        this.id = comment.getId();
        this.secret = comment.isSecret();

        boolean canView = false;
        if (currentUser != null) {
            this.isOwner = comment.getMember().getId().equals(currentUser.getId());
            // 현재 사용자가 게시글 작성자이거나 댓글 작성자인 경우
            if (comment.getPost().getMember().getId().equals(currentUser.getId()) || isOwner) {
                canView = true;
            }
        } else {
            this.isOwner = false;
        }

        if (comment.isSecret() && !canView) {
            this.nickname = "익명";
            this.content = "비밀 댓글입니다.";
        } else {
            this.nickname = comment.getNickname();
            this.content = comment.getContent();
        }
    }
}
