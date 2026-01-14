package com.example.anonymous_board.controllers.api;

import com.example.anonymous_board.domain.InquiryStatus;
import com.example.anonymous_board.dto.InquiryResponse;
import com.example.anonymous_board.dto.InquiryResponseRequest;
import com.example.anonymous_board.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminInquiryController {

    private final InquiryService inquiryService; // 문의 서비스

    // 전체 문의
    @GetMapping
    public ResponseEntity<List<InquiryResponse>> getAllInquiries(
            @RequestParam(required = false) InquiryStatus status) {

        List<InquiryResponse> inquiries;
        if (status != null) {
            inquiries = inquiryService.getInquiriesByStatus(status);
        } else {
            inquiries = inquiryService.getAllInquiries();
        }

        return ResponseEntity.ok(inquiries);
    }

    // 답변
    @PostMapping("/{id}/respond")
    public ResponseEntity<Map<String, String>> respondToInquiry(
            @PathVariable Long id,
            @RequestBody InquiryResponseRequest request) {

        inquiryService.respondToInquiry(id, request.getAdminResponse());

        Map<String, String> response = new HashMap<>();
        response.put("message", "답변이 등록되었습니다.");
        return ResponseEntity.ok(response);
    }

    // 상태 변경
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, String>> updateStatus(
            @PathVariable Long id,
            @RequestParam InquiryStatus status) {

        inquiryService.updateStatus(id, status);

        Map<String, String> response = new HashMap<>();
        response.put("message", "상태가 변경되었습니다.");
        return ResponseEntity.ok(response);
    }
}
