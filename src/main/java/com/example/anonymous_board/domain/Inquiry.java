package com.example.anonymous_board.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "inquiry")
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 문의 ID

    @Column(nullable = false, length = 200)
    private String title; // 문의 제목

    @Column(nullable = false, length = 2000)
    private String content; // 문의 내용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 문의 작성자

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status = InquiryStatus.PENDING; // 문의 상태

    @Column(length = 2000)
    private String adminResponse; // 관리자 답변

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성일

    private LocalDateTime respondedAt; // 답변일

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Inquiry(String title, String content, Member member) {
        this.title = title;
        this.content = content;
        this.member = member;
        this.status = InquiryStatus.PENDING;
    }

    public void respond(String response) {
        this.adminResponse = response;
        this.status = InquiryStatus.ANSWERED;
        this.respondedAt = LocalDateTime.now();
    }

    public void updateStatus(InquiryStatus status) {
        this.status = status;
    }
}
