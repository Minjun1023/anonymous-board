package com.example.anonymous_board.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 값 자동 증가
    private Long id;    // 아이디 게시글 고유번호

    @Column(nullable = false)
    private String nickname;    // 작성자 닉네임

    @Column(nullable = false) 
    private String title;   // 게시글 제목

    @Column(nullable = false)
    private String content; // 게시글 내용

    @Column(nullable = false, columnDefinition = "int default 0")
    private int viewCount = 0;  // 게시글 조회수

    @Column(nullable = false, columnDefinition = "int default 0")
    private int likes = 0; // 추천수

    @Column(nullable = false, columnDefinition = "int default 0")
    private int dislikes = 0; // 비추천수

    @Column(nullable = false, columnDefinition = "int default 0")
    private int commentCount = 0; // 댓글 수

    @ManyToOne(fetch = FetchType.LAZY)  // 여러 개의 게시글이 하나의 사용자에 속함. 다대일 관계
    @JoinColumn(name = "user_id")
    private Member member;  // 실제 작성자

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)  // 하나의 게시글은 여러 개의 댓글을 가질 수 있음. 일대다 관계. 게시글 삭제 시 관련된 댓글도 함께 삭제
    private List<Comment> comments = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;    // 게시글이 처음 생성된 시각

    @UpdateTimestamp
    private LocalDateTime updatedAt;    // 게시글이 수정될 때마다 자동으로 갱신되는 시간

    // 조회수 증가 시
    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementLikes() {
        this.likes++;
    }

    public void decrementLikes() {
        this.likes--;
    }

    public void incrementDislikes() {
        this.dislikes++;
    }

    public void decrementDislikes() {
        this.dislikes--;
    }

    public void incrementCommentCount() {
        this.commentCount++;
    }

    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }
}
