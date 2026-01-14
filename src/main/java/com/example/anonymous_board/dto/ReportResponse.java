package com.example.anonymous_board.dto;

import com.example.anonymous_board.domain.Report;
import com.example.anonymous_board.domain.ReportStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReportResponse {

    private final Long id;
    private final Long reporterId;
    private final String reporterNickname;
    private final Long reportedUserId;
    private final String reportedUserNickname;
    private final Long reportedPostId;
    private final String reportedPostTitle;
    private final Long reportedCommentId;
    private final String reason;
    private final ReportStatus status;
    private final String adminNotes;
    private final String processedByNickname;
    private final LocalDateTime createdAt;
    private final LocalDateTime processedAt;

    public ReportResponse(Report report) {
        this.id = report.getId();
        this.reporterId = report.getReporter().getId();
        this.reporterNickname = report.getReporter().getNickname();
        this.reportedUserId = report.getReportedUser() != null ? report.getReportedUser().getId() : null;
        this.reportedUserNickname = report.getReportedUser() != null ? report.getReportedUser().getNickname() : null;
        this.reportedPostId = report.getReportedPost() != null ? report.getReportedPost().getId() : null;
        this.reportedPostTitle = report.getReportedPost() != null ? report.getReportedPost().getTitle() : null;
        this.reportedCommentId = report.getReportedComment() != null ? report.getReportedComment().getId() : null;
        this.reason = report.getReason();
        this.status = report.getStatus();
        this.adminNotes = report.getAdminNotes();
        this.processedByNickname = report.getProcessedBy() != null ? report.getProcessedBy().getNickname() : null;
        this.createdAt = report.getCreatedAt();
        this.processedAt = report.getProcessedAt();
    }
}
