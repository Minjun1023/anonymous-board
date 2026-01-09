package com.example.anonymous_board.repository;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Poll;
import com.example.anonymous_board.domain.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// 투표
public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
    // 투표 찾기
    Optional<PollVote> findByPollAndMember(Poll poll, Member member);

    // 투표 삭제
    @Modifying
    @Query("DELETE FROM PollVote pv WHERE pv.member = :member")
    void deleteByMember(@Param("member") Member member);
}
