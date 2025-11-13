package com.example.anonymous_board.repository;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("user1") Member user1, @Param("user2") Member user2);

    List<Message> findBySenderOrReceiverOrderByCreatedAtDesc(Member sender, Member receiver);
}
