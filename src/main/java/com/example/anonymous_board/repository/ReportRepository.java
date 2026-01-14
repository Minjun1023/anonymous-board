package com.example.anonymous_board.repository;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Report;
import com.example.anonymous_board.domain.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    // 상태별 신고 조회 (페이징)
    Page<Report> findAllByStatus(ReportStatus status, Pageable pageable);

    // 특정 사용자에 대한 신고 목록
    List<Report> findAllByReportedUser(Member user);

    // 특정 사용자에 대한 특정 상태의 신고 수
    long countByReportedUserAndStatus(Member user, ReportStatus status);

    // 모든 신고 조회 (페이징)
    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
