package com.example.anonymous_board.repository;

import com.example.anonymous_board.domain.BoardType;
import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Post;

import io.micrometer.common.lang.NonNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByMemberOrderByCreatedAtDesc(Member member); // 회원의 게시글 조회

    // 회원의 게시글 조회 (페이지네이션)
    Page<Post> findByMember(Member member, Pageable pageable);

    int countByMember(Member member); // 회원의 게시글 개수

    // 페이지네이션을 위한 메서드
    Page<Post> findAll(@NonNull Pageable pageable); // 게시글 전체 조회

    @Modifying
    @Query("UPDATE Post p SET p.nickname = :nickname WHERE p.member = :member")
    void updateNicknameByMember(@Param("member") Member member,
            @Param("nickname") String nickname);

    // 검색 기능 (제목 또는 내용에 키워드 포함)
    Page<Post> findByTitleContainingOrContentContaining(String titleKeyword, String contentKeyword, Pageable pageable);

    // 핫 게시글 조회 (네트 스코어 기준: 추천수 - 비추천수, 네트 스코어 순 정렬)
    @Query("SELECT p FROM Post p WHERE (p.likes - p.dislikes) >= :minCount ORDER BY (p.likes - p.dislikes) DESC")
    Page<Post> findHotPosts(@Param("minCount") int minCount, Pageable pageable);

    // 공지사항 조회 (최신순)
    @Query("SELECT p FROM Post p WHERE p.isAnnouncement = true ORDER BY p.createdAt DESC")
    List<Post> findAnnouncements();

    // 일반 게시글 조회 (페이징 + 정렬)
    @Query("SELECT p FROM Post p WHERE p.isAnnouncement = false")
    Page<Post> findNonAnnouncementPosts(Pageable pageable);

    // 검색 시 공지사항 조회
    @Query("SELECT p FROM Post p WHERE p.isAnnouncement = true AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) ORDER BY p.createdAt DESC")
    List<Post> findAnnouncementsByKeyword(@Param("keyword") String keyword);

    // 검색 시 일반 게시글 조회
    @Query("SELECT p FROM Post p WHERE p.isAnnouncement = false AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
    Page<Post> findNonAnnouncementsByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 회원 탈퇴 시 게시글 삭제
    @Modifying
    @Query("DELETE FROM Post p WHERE p.member = :member")
    void deleteByMember(@Param("member") Member member);

    @Query("SELECT p FROM Post p WHERE p.boardType = :boardType ORDER BY p.createdAt DESC")
    Page<Post> findByBoardType(@Param("boardType") BoardType boardType, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.boardType = :boardType AND p.isAnnouncement = false ORDER BY p.createdAt DESC")
    Page<Post> findNonAnnouncementPostsByBoardType(@Param("boardType") BoardType boardType, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isAnnouncement = true AND p.boardType = :boardType")
    List<Post> findAnnouncementsByBoardType(@Param("boardType") BoardType boardType);
}
