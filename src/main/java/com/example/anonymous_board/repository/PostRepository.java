package com.example.anonymous_board.repository;

import com.example.anonymous_board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
