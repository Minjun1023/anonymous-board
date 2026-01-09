package com.example.anonymous_board.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 투표(설문) 엔티티
 * 게시글에 포함되는 투표 기능을 담당
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Poll {

    // 투표 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 투표 질문
    @Column(nullable = false)
    private String question;

    // 이 투표가 속한 게시글 (1:1 관계)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    // 투표 선택지 목록
    // Poll 삭제 시 모든 PollOption도 자동 삭제됨
    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PollOption> options = new ArrayList<>();

    public Poll(String question, Post post) {
        this.question = question;
        this.post = post;
    }

    public void addOption(PollOption option) {
        this.options.add(option); // Poll의 options 리스트에 추가
        option.setPoll(this); // PollOption에 Poll 참조 설정
    }
}
