package com.example.anonymous_board.repository;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("user1") Member user1, @Param("user2") Member user2); // 대화 찾기

    List<Message> findBySenderOrReceiverOrderByCreatedAtDesc(Member sender, Member receiver); // 보내거나 받은 메시지 찾기

    @Modifying
    @Query("DELETE FROM Message m WHERE m.sender = :sender")
    void deleteBySender(@Param("sender") Member sender); // 보내는 사람이 삭제할 때 메시지 삭제

    @Modifying
    @Query("DELETE FROM Message m WHERE m.receiver = :receiver")
    void deleteByReceiver(@Param("receiver") Member receiver); // 받는 사람이 삭제할 때 메시지 삭제
}
