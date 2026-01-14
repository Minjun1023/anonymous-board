package com.example.anonymous_board.domain;

// 문의 상태
public enum InquiryStatus {
    PENDING("대기 중"),
    ANSWERED("답변 완료"),
    CLOSED("종료");

    private final String title;

    InquiryStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
