package com.example.anonymous_board.dto;

import com.example.anonymous_board.domain.VoteType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteRequest {
    private VoteType voteType; // 투표 타입
}
