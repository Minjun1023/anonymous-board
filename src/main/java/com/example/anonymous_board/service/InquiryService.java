package com.example.anonymous_board.service;

import com.example.anonymous_board.domain.Inquiry;
import com.example.anonymous_board.domain.InquiryStatus;
import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.dto.InquiryCreateRequest;
import com.example.anonymous_board.dto.InquiryResponse;
import com.example.anonymous_board.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    // 문의 생성
    @Transactional
    public InquiryResponse createInquiry(InquiryCreateRequest request, Member member) {
        Inquiry inquiry = new Inquiry(request.getTitle(), request.getContent(), member);
        Inquiry saved = inquiryRepository.save(inquiry);
        return InquiryResponse.from(saved);
    }

    // 전체 문의
    public List<InquiryResponse> getAllInquiries() {
        return inquiryRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(InquiryResponse::from)
                .collect(Collectors.toList());
    }

    // 상태별 문의
    public List<InquiryResponse> getInquiriesByStatus(InquiryStatus status) {
        return inquiryRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(InquiryResponse::from)
                .collect(Collectors.toList());
    }

    // 회원별 문의
    public List<InquiryResponse> getUserInquiries(Member member) {
        return inquiryRepository.findByMemberOrderByCreatedAtDesc(member).stream()
                .map(InquiryResponse::from)
                .collect(Collectors.toList());
    }

    // 회원별 문의 (페이지네이션)
    public Page<InquiryResponse> getUserInquiriesPaged(Member member, int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC,
                        "createdAt"));

        return inquiryRepository.findByMember(member, pageable)
                .map(InquiryResponse::from);
    }

    // 답변
    @Transactional
    public InquiryResponse respondToInquiry(Long inquiryId, String response) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));

        inquiry.respond(response);
        return InquiryResponse.from(inquiry);
    }

    // 상태 변경
    @Transactional
    public InquiryResponse updateStatus(Long inquiryId, InquiryStatus status) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));

        inquiry.updateStatus(status);
        return InquiryResponse.from(inquiry);
    }
}
