package com.example.anonymous_board.dto;

import com.example.anonymous_board.domain.PollOption;
import lombok.Getter;

@Getter
public class PollOptionResponse {
    private final Long id; // 투표 옵션 ID
    private final String text; // 투표 옵션 텍스트
    private final int voteCount; // 투표 수
    private final double votePercentage; // 투표 비율

    public PollOptionResponse(PollOption option, long totalVotes) {
        this.id = option.getId();
        this.text = option.getText();
        this.voteCount = option.getVoteCount();
        this.votePercentage = totalVotes > 0 ? (double) option.getVoteCount() / totalVotes * 100 : 0;
    }
}
