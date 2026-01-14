package com.example.anonymous_board.repository;

import com.example.anonymous_board.domain.Inquiry;
import com.example.anonymous_board.domain.InquiryStatus;
import com.example.anonymous_board.domain.Member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    List<Inquiry> findAllByOrderByCreatedAtDesc(); // 생성일 기준 내림차순

    List<Inquiry> findByMemberOrderByCreatedAtDesc(Member member); // 회원별 문의 내림차순

    Page<Inquiry> findByMember(Member member,
            Pageable pageable); // 회원별 문의 페이징

    List<Inquiry> findByStatusOrderByCreatedAtDesc(InquiryStatus status); // 상태별 문의 내림차순
}
