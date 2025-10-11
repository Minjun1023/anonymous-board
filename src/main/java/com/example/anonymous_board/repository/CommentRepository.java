package com.example.anonymous_board.repository;

import com.example.anonymous_board.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
