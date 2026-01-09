package com.example.anonymous_board.repository;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.UserConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserConversationRepository extends JpaRepository<UserConversation, Long> {
    Optional<UserConversation> findByUserAndOtherUser(Member user, Member otherUser); // 사용자와 다른 사용자의 대화방 조회

    @Modifying
    @Query("DELETE FROM UserConversation uc WHERE uc.user = :user")
    void deleteByUser(@Param("user") Member user); // 사용자 탈퇴 시 사용자의 대화방 삭제

    @Modifying
    @Query("DELETE FROM UserConversation uc WHERE uc.otherUser = :otherUser")
    void deleteByOtherUser(@Param("otherUser") Member otherUser); // 다른 사용자 탈퇴 시 다른 사용자의 대화방 삭제
}
