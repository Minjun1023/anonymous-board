package com.example.anonymous_board.repository;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Post;
import com.example.anonymous_board.domain.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByMemberAndPost(Member member, Post post); // 회원과 게시글로 투표 조회

    @Modifying
    @Query("DELETE FROM Vote v WHERE v.member = :member") // 회원 탈퇴 시 투표 삭제
    void deleteByMember(@Param("member") Member member);

    @Modifying
    @Query("DELETE FROM Vote v WHERE v.post.member = :member") // 게시글 작성자 탈퇴 시 투표 삭제
    void deleteByPostMember(@Param("member") Member member);
}
