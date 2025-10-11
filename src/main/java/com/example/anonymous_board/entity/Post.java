package com.example.anonymous_board.entity;

import com.example.anonymous_board.domain.Comment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 값 자동 생성
    private Long id;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false) // null 값 허용 X
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String password;    // 수정 비밀번호

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
}
