package com.example.anonymous_board.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 게시글 이미지 엔티티
 * 게시글에 첨부된 이미지 파일 정보를 저장
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class PostImage {

    // 이미지 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl; // 이미지 URL

    // 이미지가 속한 게시글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    public PostImage(String imageUrl, Post post) {
        this.imageUrl = imageUrl;
        this.post = post;
    }
}
