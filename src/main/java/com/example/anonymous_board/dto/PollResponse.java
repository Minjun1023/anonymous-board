package com.example.anonymous_board.dto;

import com.example.anonymous_board.domain.Poll;
import com.example.anonymous_board.domain.PollOption;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PollResponse {
    private final Long id; // 투표 ID
    private final String question; // 투표 질문
    private final List<PollOptionResponse> options; // 투표 옵션 리스트
    private final Long votedOptionId; // 현재 사용자가 투표한 항목 ID (없으면 null)

    public PollResponse(Poll poll, Long votedOptionId) {
        this.id = poll.getId();
        this.question = poll.getQuestion();

        long totalVotes = poll.getOptions().stream().mapToLong(PollOption::getVoteCount).sum(); // 투표 총수

        this.options = poll.getOptions().stream()
                .map(option -> new PollOptionResponse(option, totalVotes))
                .collect(Collectors.toList());

        this.votedOptionId = votedOptionId;
    }
}
