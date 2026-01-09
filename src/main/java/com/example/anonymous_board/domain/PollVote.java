package com.example.anonymous_board.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 투표 기록 엔티티
 * 사용자가 어떤 투표의 어떤 선택지에 투표했는지 기록
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class PollVote {

    // 투표 기록 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 투표에 참여했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id")
    private Poll poll;

    // 누가 투표했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;

    // 어떤 선택지를 선택했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private PollOption option;

    public PollVote(Poll poll, Member member, PollOption option) {
        this.poll = poll;
        this.member = member;
        this.option = option;
    }
}
