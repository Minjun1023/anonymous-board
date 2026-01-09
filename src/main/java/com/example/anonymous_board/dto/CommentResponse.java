package com.example.anonymous_board.dto;

import com.example.anonymous_board.domain.Comment;
import com.example.anonymous_board.domain.Member;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CommentResponse {
    private final Long id; // 댓글 ID
    private final String nickname; // 댓글 작성자 닉네임
    private final String content; // 댓글 내용
    private final boolean secret; // 비밀 댓글 여부
    @JsonProperty("isOwner") // 직렬화: 프론트 엔드와 코드 통일 Owner -> isOwner
    private final boolean isOwner; // 댓글 작성자 여부
    private final List<CommentResponse> children; // 자식 댓글 리스트
    private final String profileImage; // 댓글 작성자 프로필 이미지

    public CommentResponse(Comment comment, Member currentUser, Map<Long, Integer> anonymousMap) {
        this.id = comment.getId(); // 댓글 ID
        this.secret = comment.isSecret(); // 비밀 댓글 여부

        boolean canView = false; // 댓글 조회 권한 여부
        // 현재 사용자가 게시글 작성자이거나 댓글 작성자인 경우
        if (currentUser != null) {
            this.isOwner = comment.getMember().getId().equals(currentUser.getId());
            // 현재 사용자가 게시글 작성자이거나 댓글 작성자인 경우
            if (comment.getPost().getMember().getId().equals(currentUser.getId()) || isOwner) {
                canView = true;
            }
        } else {
            this.isOwner = false; // 댓글 작성자 여부
        }

        if (comment.isSecret() && !isOwner) {
            // 비밀 댓글인 경우 익명으로 표시
            Integer anonymousId = anonymousMap.get(comment.getMember().getId());
            this.nickname = "익명 " + (anonymousId != null ? anonymousId : "?");
            this.content = canView ? comment.getContent() : "비밀 댓글입니다.";
            // 비밀 댓글인 경우 기본 프로필 이미지 사용
            this.profileImage = "/profiles/default_profile.png";
        } else {
            // 일반 댓글인 경우
            this.nickname = comment.getNickname();
            // 댓글 내용
            this.content = comment.getContent();
            // 댓글 작성자 프로필 이미지
            String image = comment.getMember().getProfileImage();
            if (image != null && !image.startsWith("/profiles/") && !image.startsWith("http")) {
                this.profileImage = "/profiles/" + image;
            } else {
                this.profileImage = image;
            }
        }

        // 자식 댓글 리스트
        this.children = comment.getChildren().stream()
                .map(child -> new CommentResponse(child, currentUser, anonymousMap))
                .collect(Collectors.toList());
    }
}
