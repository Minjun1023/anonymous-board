package com.example.anonymous_board.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InquiryCreateRequest {
    private String title; // 문의 제목
    private String content; // 문의 내용

    public InquiryCreateRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}