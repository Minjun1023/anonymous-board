package com.example.anonymous_board.domain;

import com.example.anonymous_board.entity.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String password;

    @ManyToOne // 여러 Comment 하나의 Post에 속하므로, 다대일 관계
    @JoinColumn(name = "post_id") // 외래 키 칼럼 이름을 'post_id'로 지정
    private Post post;

}
