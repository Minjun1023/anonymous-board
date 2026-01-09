package com.example.anonymous_board.dto;

import com.example.anonymous_board.domain.Comment;
import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Post;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class PostResponse {
    private final Long id; // 게시글 ID
    private final Long authorId; // 게시글 작성자 ID
    private final String nickname; // 게시글 작성자 닉네임
    private final String title; // 게시글 제목
    private final String content; // 게시글 내용
    private final LocalDateTime createdAt; // 게시글 생성 시간
    private final int viewCount; // 게시글 조회수
    private final int likes; // 게시글 좋아요 수
    private final int dislikes; // 게시글 싫어요 수
    private final int commentCount; // 게시글 댓글 수
    @JsonProperty("isOwner")
    private final boolean isOwner; // 게시글 작성자 여부
    private final List<CommentResponse> comments; // 게시글 댓글 리스트
    private final List<PostImageResponse> images; // 게시글 이미지 리스트
    private final PollResponse poll; // 게시글 투표 정보
    private final String profileImage; // 게시글 작성자 프로필 이미지

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

        String image = post.getMember().getProfileImage();
        if (image != null && !image.startsWith("/profiles/") && !image.startsWith("http")) {
            this.profileImage = "/profiles/" + image;
        } else {
            this.profileImage = image;
        }

        // 익명 ID 부여 로직
        List<Comment> allComments = post.getComments();
        allComments.sort((c1, c2) -> c1.getCreatedAt().compareTo(c2.getCreatedAt()));
        Map<Long, Integer> anonymousMap = new HashMap<>();
        int nextAnonymousId = 1;
        for (Comment comment : allComments) {
            Long memberId = comment.getMember().getId();
            if (!anonymousMap.containsKey(memberId)) {
                anonymousMap.put(memberId, nextAnonymousId++);
            }
        }

        this.comments = post.getComments().stream()
                .filter(comment -> comment.getParent() == null) // 최상위 댓글만 변환 (자식은 내부에서 처리)
                .map(comment -> new CommentResponse(comment, currentUser, anonymousMap))
                .collect(Collectors.toList());
        this.images = post.getImages().stream()
                .map(PostImageResponse::new)
                .collect(Collectors.toList());

        if (post.getPoll() != null) {
            this.poll = new PollResponse(post.getPoll(), null);
        } else {
            this.poll = null;
        }
    }
}
