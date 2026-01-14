package com.example.anonymous_board.dto;

import com.example.anonymous_board.domain.ReportStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReportProcessRequest {

    private ReportStatus status; // 새로운 상태
    private String adminNotes; // 관리자 메모
    private boolean suspendUser; // 사용자 정지 여부
    private Integer suspendDays; // 정지 일수 (null이면 영구 정지)
    private String suspensionReason; // 정지 사유
}
