package com.example.anonymous_board.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 투표 선택지 엔티티
 * Poll의 각 선택 항목을 표현
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class PollOption {

    // 선택지 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 선택지 텍스트
    @Column(nullable = false)
    private String text;

    // 이 선택지가 속한 투표
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id")
    private Poll poll;

    // 이 선택지에 투표한 사용자들의 투표 기록
    // PollOption 삭제 시 모든 PollVote도 자동 삭제됨
    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PollVote> votes = new ArrayList<>();

    // 현재 투표 수 (성능 최적화를 위해 별도 저장)
    @Column(nullable = false)
    private int voteCount = 0;

    public PollOption(String text) {
        this.text = text;
    }
}
