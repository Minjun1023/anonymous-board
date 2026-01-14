package com.example.anonymous_board.service;

import com.example.anonymous_board.domain.*;
import com.example.anonymous_board.dto.ReportCreateRequest;
import com.example.anonymous_board.dto.ReportProcessRequest;
import com.example.anonymous_board.dto.ReportResponse;
import com.example.anonymous_board.repository.CommentRepository;
import com.example.anonymous_board.repository.PostRepository;
import com.example.anonymous_board.repository.ReportRepository;
import com.example.anonymous_board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    // 게시글 신고
    @Transactional
    public ReportResponse reportPost(Long postId, ReportCreateRequest request, Member reporter) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 자기 자신의 게시글은 신고할 수 없음
        if (post.getMember().getId().equals(reporter.getId())) {
            throw new IllegalArgumentException("자신의 게시글은 신고할 수 없습니다.");
        }

        Report report = new Report(reporter, post, request.getReason());
        reportRepository.save(report);

        return new ReportResponse(report);
    }

    // 댓글 신고
    @Transactional
    public ReportResponse reportComment(Long commentId, ReportCreateRequest request, Member reporter) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 자기 자신의 댓글은 신고할 수 없음
        if (comment.getMember().getId().equals(reporter.getId())) {
            throw new IllegalArgumentException("자신의 댓글은 신고할 수 없습니다.");
        }

        Report report = new Report(reporter, comment, request.getReason());
        reportRepository.save(report);

        return new ReportResponse(report);
    }

    // 사용자 신고
    @Transactional
    public ReportResponse reportUser(Long userId, ReportCreateRequest request, Member reporter) {
        Member reportedUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 자기 자신은 신고할 수 없음
        if (reportedUser.getId().equals(reporter.getId())) {
            throw new IllegalArgumentException("자신을 신고할 수 없습니다.");
        }

        Report report = new Report(reporter, reportedUser, request.getReason());
        reportRepository.save(report);

        return new ReportResponse(report);
    }

    // 신고 목록 조회 (상태별 필터)
    public Page<ReportResponse> getReports(ReportStatus status, Pageable pageable) {
        Page<Report> reports;
        if (status != null) {
            reports = reportRepository.findAllByStatus(status, pageable);
        } else {
            reports = reportRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return reports.map(ReportResponse::new);
    }

    // 신고 상세 조회
    public ReportResponse getReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다."));
        return new ReportResponse(report);
    }

    // 신고 처리
    @Transactional
    public ReportResponse processReport(Long reportId, ReportProcessRequest request, Member admin) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다."));

        // 신고 상태 업데이트
        report.process(admin, request.getStatus(), request.getAdminNotes());

        // 사용자 정지 처리
        if (request.isSuspendUser() && report.getReportedUser() != null) {
            Member reportedUser = report.getReportedUser();
            LocalDateTime suspendUntil = null;

            if (request.getSuspendDays() != null && request.getSuspendDays() > 0) {
                suspendUntil = LocalDateTime.now().plusDays(request.getSuspendDays());
            }

            reportedUser.suspend(suspendUntil, request.getSuspensionReason());
            userRepository.save(reportedUser);
        }

        reportRepository.save(report);
        return new ReportResponse(report);
    }

    // 신고 통계
    public Map<String, Object> getReportStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalReports = reportRepository.count();
        long pendingReports = reportRepository.countByReportedUserAndStatus(null, ReportStatus.PENDING);

        stats.put("total", totalReports);
        stats.put("pending", pendingReports);
        stats.put("resolved", reportRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReportStatus.RESOLVED).count());
        stats.put("rejected", reportRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReportStatus.REJECTED).count());

        return stats;
    }
}
