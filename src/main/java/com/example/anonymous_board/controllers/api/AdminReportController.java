package com.example.anonymous_board.controllers.api;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.ReportStatus;
import com.example.anonymous_board.dto.ReportProcessRequest;
import com.example.anonymous_board.dto.ReportResponse;
import com.example.anonymous_board.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final ReportService reportService;

    // 신고 목록 조회 (필터링 가능)
    @GetMapping
    public ResponseEntity<Page<ReportResponse>> getReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ReportResponse> reports = reportService.getReports(status, pageable);
        return ResponseEntity.ok(reports);
    }

    // 신고 상세 조회
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponse> getReport(@PathVariable Long reportId) {
        ReportResponse report = reportService.getReport(reportId);
        return ResponseEntity.ok(report);
    }

    // 신고 처리
    @PostMapping("/{reportId}/process")
    public ResponseEntity<ReportResponse> processReport(
            @PathVariable Long reportId,
            @RequestBody ReportProcessRequest request,
            @AuthenticationPrincipal Member admin) {

        ReportResponse response = reportService.processReport(reportId, request, admin);
        return ResponseEntity.ok(response);
    }

    // 신고 통계
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = reportService.getReportStatistics();
        return ResponseEntity.ok(stats);
    }
}
