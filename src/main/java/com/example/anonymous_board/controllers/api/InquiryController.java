package com.example.anonymous_board.controllers.api;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.dto.InquiryCreateRequest;
import com.example.anonymous_board.dto.InquiryResponse;
import com.example.anonymous_board.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService; // 문의 서비스

    // 문의 생성
    @PostMapping
    public ResponseEntity<Map<String, String>> createInquiry(
            @RequestBody InquiryCreateRequest request,
            @AuthenticationPrincipal Member member) {

        if (member == null) {
            return ResponseEntity.status(401).build();
        }

        inquiryService.createInquiry(request, member);

        Map<String, String> response = new HashMap<>();
        response.put("message", "문의가 성공적으로 접수되었습니다. 관리자 확인 후 답변드리겠습니다.");
        return ResponseEntity.ok(response);
    }

    // 내 문의 조회 (페이지네이션 지원)
    @GetMapping("/my")
    public ResponseEntity<?> getMyInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Member member) {

        if (member == null) {
            return ResponseEntity.status(401).build();
        }

        // 페이지네이션 적용
        org.springframework.data.domain.Page<InquiryResponse> inquiriesPage = inquiryService
                .getUserInquiriesPaged(member, page, size);

        return ResponseEntity.ok(inquiriesPage);
    }
}
