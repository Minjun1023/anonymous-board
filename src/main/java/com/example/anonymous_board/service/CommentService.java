package com.example.anonymous_board.service;

import com.example.anonymous_board.domain.Comment;
import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Post;
import com.example.anonymous_board.dto.CommentCreateRequest;
import com.example.anonymous_board.dto.CommentResponse;
import com.example.anonymous_board.dto.CommentUpdateRequest;
import com.example.anonymous_board.repository.CommentRepository;
import com.example.anonymous_board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    // 1. 댓글 목록 조회
    public List<CommentResponse> getCommentsByPostId(Long postId, Member currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        return post.getComments().stream()
                .map(comment -> new CommentResponse(comment, currentUser))
                .collect(Collectors.toList());
    }

    // 2. 댓글 작성
    @Transactional
    public void createComment(Long postId, CommentCreateRequest request, Member currentUser) {
        // postId로 게시글을 찾음
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Comment comment = new Comment();
        comment.setNickname(currentUser.getNickname());
        comment.setContent(request.getContent());
        comment.setSecret(request.isSecret()); // 비밀 댓글 여부 설정
        comment.setMember(currentUser);  // 실제 작성자 설정
        comment.setPost(post);  // 게시글에 댓글 연결

        commentRepository.save(comment);
        
        // 게시글의 댓글 수 증가
        post.incrementCommentCount();
        postRepository.save(post);
    }

    // 3. 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Member currentUser) {
        // commentId로 댓글을 찾는다.
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 본인 확인
        if (!comment.getMember().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        // 게시글의 댓글 수 감소
        Post post = comment.getPost();
        post.decrementCommentCount();
        postRepository.save(post);

        commentRepository.delete(comment);
    }

    // 4. 댓글 수정
    @Transactional
    public void updateComment(Long commentId, CommentUpdateRequest request, Member currentUser) {
        // 1. commentId로 댓글 찾기
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 2. 본인 확인
        if (!comment.getMember().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        // 3. 전달받은 내용으로 댓글 업데이트
        comment.setNickname(currentUser.getNickname());
        comment.setContent(request.getContent());
    }
}
