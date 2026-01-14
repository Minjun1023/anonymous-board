package com.example.anonymous_board.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 신고 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter; // 신고자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private Member reportedUser; // 신고된 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_post_id")
    private Post reportedPost; // 신고된 게시글 (nullable)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_comment_id")
    private Comment reportedComment; // 신고된 댓글 (nullable)

    @Column(nullable = false, length = 1000)
    private String reason; // 신고 사유

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING; // 신고 상태

    @Column(length = 1000)
    private String adminNotes; // 관리자 메모

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_id")
    private Member processedBy; // 처리한 관리자

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt; // 신고 생성 시간

    private LocalDateTime processedAt; // 처리 시간

    // 게시글 신고 생성자
    public Report(Member reporter, Post reportedPost, String reason) {
        this.reporter = reporter;
        this.reportedPost = reportedPost;
        this.reportedUser = reportedPost.getMember();
        this.reason = reason;
        this.status = ReportStatus.PENDING;
    }

    // 댓글 신고 생성자
    public Report(Member reporter, Comment reportedComment, String reason) {
        this.reporter = reporter;
        this.reportedComment = reportedComment;
        this.reportedUser = reportedComment.getMember();
        this.reason = reason;
        this.status = ReportStatus.PENDING;
    }

    // 사용자 신고 생성자
    public Report(Member reporter, Member reportedUser, String reason) {
        this.reporter = reporter;
        this.reportedUser = reportedUser;
        this.reason = reason;
        this.status = ReportStatus.PENDING;
    }

    // 신고 처리
    public void process(Member admin, ReportStatus newStatus, String notes) {
        this.processedBy = admin;
        this.status = newStatus;
        this.adminNotes = notes;
        this.processedAt = LocalDateTime.now();
    }
}
