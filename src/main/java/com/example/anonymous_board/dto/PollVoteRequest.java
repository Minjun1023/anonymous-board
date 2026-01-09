package com.example.anonymous_board.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PollVoteRequest {
    private Long optionId; // 투표 옵션 ID
}
