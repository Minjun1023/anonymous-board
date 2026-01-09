package com.example.anonymous_board.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 아이디 (기본 키)

    @Column(nullable = false)
    private String nickname; // 닉네임

    @Column(nullable = false)
    private String content; // 본문 내용

    @ManyToOne(fetch = FetchType.LAZY) // 여러 Comment 하나의 Member에 속하므로 다대일 관계, 댓글 불러올 때 사용자 정보는 필요할 때에 DB에서 가져오므로 지연로딩
    @JoinColumn(name = "user_id")
    private Member member; // 실제 작성자

    @ManyToOne // 여러 Comment 하나의 Post에 속하므로, 다대일 관계
    @JoinColumn(name = "post_id") // 외래 키 칼럼 이름을 'post_id'로 지정
    @JsonIgnore // 순환 참조 방지
    private Post post; // 게시글

    @Column(nullable = false)
    private boolean secret = false; // 비밀 댓글 여부

    @CreationTimestamp // 댓글이 생성된 시간 자동 저장
    @Column(updatable = false)
    private LocalDateTime createdAt; // 생성 시간

    @UpdateTimestamp // 댓글이 수정된 시간 자동 저장
    private LocalDateTime updatedAt; // 수정 시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent; // 부모 댓글

    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private java.util.List<Comment> children = new java.util.ArrayList<>(); // 자식 댓글들
}
