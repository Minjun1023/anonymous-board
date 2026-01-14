package com.example.anonymous_board.controllers.api;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.dto.ReportCreateRequest;
import com.example.anonymous_board.dto.ReportResponse;
import com.example.anonymous_board.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 게시글 신고
    @PostMapping("/post/{postId}")
    public ResponseEntity<ReportResponse> reportPost(
            @PathVariable Long postId,
            @RequestBody ReportCreateRequest request,
            @AuthenticationPrincipal Member reporter) {

        if (reporter == null) {
            return ResponseEntity.status(401).build();
        }

        ReportResponse response = reportService.reportPost(postId, request, reporter);
        return ResponseEntity.ok(response);
    }

    // 댓글 신고
    @PostMapping("/comment/{commentId}")
    public ResponseEntity<ReportResponse> reportComment(
            @PathVariable Long commentId,
            @RequestBody ReportCreateRequest request,
            @AuthenticationPrincipal Member reporter) {

        if (reporter == null) {
            return ResponseEntity.status(401).build();
        }

        ReportResponse response = reportService.reportComment(commentId, request, reporter);
        return ResponseEntity.ok(response);
    }

    // 사용자 신고
    @PostMapping("/user/{userId}")
    public ResponseEntity<ReportResponse> reportUser(
            @PathVariable Long userId,
            @RequestBody ReportCreateRequest request,
            @AuthenticationPrincipal Member reporter) {

        if (reporter == null) {
            return ResponseEntity.status(401).build();
        }

        ReportResponse response = reportService.reportUser(userId, request, reporter);
        return ResponseEntity.ok(response);
    }
}
