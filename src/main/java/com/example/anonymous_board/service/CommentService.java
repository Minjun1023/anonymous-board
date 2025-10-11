package com.example.anonymous_board.service;

import com.example.anonymous_board.domain.Comment;
import com.example.anonymous_board.dto.CommentCreateRequest;
import com.example.anonymous_board.dto.CommentUpdateRequest;
import com.example.anonymous_board.entity.Post;
import com.example.anonymous_board.repository.CommentRepository;
import com.example.anonymous_board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    // 1. 댓글 작성
    @Transactional
    public void createComment(Long postId, CommentCreateRequest request) {
        // postId로 게시글을 찾음
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Comment comment = new Comment();
        comment.setNickname(request.getNickname());
        comment.setContent(request.getContent());
        comment.setPassword(request.getPassword());
        comment.setPost(post);  // 게시글에 댓글 연결

        commentRepository.save(comment);
    }

    // 2. 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, String password) {
        // commentId로 댓글을 찾는다.
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 비밀번호 확인
        if (!comment.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        commentRepository.delete(comment);
    }

    // 3. 댓글 수정
    @Transactional
    public void updateComment(Long commentId, CommentUpdateRequest request) {
        // 1. commentId로 댓글 찾기
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 2. 비밀번호 확인
        if (!comment.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 전달받은 내용으로 댓글 업데이트
        comment.setNickname(request.getNickname());
        comment.setContent(request.getContent());
    }
}
