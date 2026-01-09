package com.example.anonymous_board.repository;

import com.example.anonymous_board.domain.Comment;
import com.example.anonymous_board.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByMemberOrderByCreatedAtDesc(Member member); // 회원의 댓글 조회

    // 회원의 댓글 조회 (페이지네이션)
    Page<Comment> findByMember(Member member, Pageable pageable);

    int countByMember(Member member); // 회원의 댓글 수 조회

    // 회원의 닉네임 변경
    @Modifying
    @Query("UPDATE Comment c SET c.nickname = :nickname WHERE c.member = :member")
    void updateNicknameByMember(@Param("member") Member member,
            @Param("nickname") String nickname);

    // 회원 탈퇴 시 외래키 체크 비활성화
    @Modifying
    @Query(value = "SET FOREIGN_KEY_CHECKS=0", nativeQuery = true)
    void disableForeignKeyChecks();

    // 회원 탈퇴 시 모든 관련 댓글 삭제
    @Modifying
    @Query(value = "DELETE FROM comment WHERE user_id = :userId OR post_id IN (SELECT id FROM post WHERE user_id = :userId)", nativeQuery = true)
    void deleteAllCommentsByUser(@Param("userId") Long userId);

    // 회원 탈퇴 시 외래키 체크 활성화
    @Modifying
    @Query(value = "SET FOREIGN_KEY_CHECKS=1", nativeQuery = true)
    void enableForeignKeyChecks();
}
