package com.example.anonymous_board.domain;

public enum ReportStatus {
    PENDING, // 대기 중 (관리자 검토 필요)
    REVIEWED, // 검토됨 (조치 대기)
    RESOLVED, // 해결됨 (조치 완료)
    REJECTED // 기각됨 (부적절한 신고)
}
