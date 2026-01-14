package com.example.anonymous_board.dto;

import com.example.anonymous_board.domain.Inquiry;
import com.example.anonymous_board.domain.InquiryStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InquiryResponse {
    private Long id; // 문의 ID
    private String title; // 문의 제목
    private String content; // 문의 내용
    private InquiryStatus status; // 문의 상태
    private String adminResponse; // 관리자 답변
    private LocalDateTime createdAt; // 생성일
    private LocalDateTime respondedAt; // 답변일

    // 유저 정보
    private Long userId; // 회원 ID
    private String userEmail; // 회원 이메일
    private String userNickname; // 회원 닉네임

    public static InquiryResponse from(Inquiry inquiry) {
        return InquiryResponse.builder()
                .id(inquiry.getId())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .status(inquiry.getStatus())
                .adminResponse(inquiry.getAdminResponse())
                .createdAt(inquiry.getCreatedAt())
                .respondedAt(inquiry.getRespondedAt())
                .userId(inquiry.getMember().getId())
                .userEmail(inquiry.getMember().getEmail())
                .userNickname(inquiry.getMember().getNickname())
                .build();
    }
}
