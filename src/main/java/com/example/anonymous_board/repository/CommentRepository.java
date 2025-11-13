package com.example.anonymous_board.repository;

import com.example.anonymous_board.domain.Comment;
import com.example.anonymous_board.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByMemberOrderByCreatedAtDesc(Member member);
    int countByMember(Member member);
}
