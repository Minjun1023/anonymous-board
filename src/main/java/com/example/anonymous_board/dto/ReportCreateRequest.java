package com.example.anonymous_board.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReportCreateRequest {

    private String reason; // 신고 사유

    public ReportCreateRequest(String reason) {
        this.reason = reason;
    }
}
