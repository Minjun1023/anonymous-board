package com.example.anonymous_board.repository;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByMemberOrderByCreatedAtDesc(Member member);
    int countByMember(Member member);
    
    // 페이지네이션을 위한 메서드
    Page<Post> findAll(Pageable pageable);
}
