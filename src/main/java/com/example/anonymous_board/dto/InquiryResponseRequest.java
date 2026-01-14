package com.example.anonymous_board.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InquiryResponseRequest {
    private String adminResponse; // 관리자 답변

    public InquiryResponseRequest(String adminResponse) {
        this.adminResponse = adminResponse;
    }
}
