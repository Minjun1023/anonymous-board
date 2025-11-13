package com.example.anonymous_board.repository;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.UserConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserConversationRepository extends JpaRepository<UserConversation, Long> {
    Optional<UserConversation> findByUserAndOtherUser(Member user, Member otherUser);
}
