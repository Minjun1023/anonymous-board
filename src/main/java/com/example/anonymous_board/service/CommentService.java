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

    // 1. 댓글 목록 조회 (계층형 구조로 반환)
    public List<CommentResponse> getCommentsByPostId(Long postId, Member currentUser) {
        if (postId == null)
            throw new IllegalArgumentException("Post ID cannot be null");
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 익명 ID 부여 로직
        // 게시글에 달린 모든 댓글(대댓글 포함)을 가져와서 작성자별로 번호 부여
        List<Comment> allComments = post.getComments(); // 이미 로딩됨 (Lazy지만 접근 시 로딩)
        // 작성일 순으로 정렬 (순서 보장을 위해)
        allComments.sort((c1, c2) -> c1.getCreatedAt().compareTo(c2.getCreatedAt()));

        java.util.Map<Long, Integer> anonymousMap = new java.util.HashMap<>();
        int nextAnonymousId = 1;

        for (Comment comment : allComments) {
            Long memberId = comment.getMember().getId();
            if (!anonymousMap.containsKey(memberId)) {
                anonymousMap.put(memberId, nextAnonymousId++);
            }
        }

        // 부모가 없는 최상위 댓글만 반환 (자식 댓글은 CommentResponse 내부에서 재귀적으로 처리됨)
        return post.getComments().stream()
                .filter(comment -> comment.getParent() == null)
                .map(comment -> new CommentResponse(comment, currentUser, anonymousMap))
                .collect(Collectors.toList());
    }

    // 2. 댓글 작성
    @Transactional
    public void createComment(Long postId, CommentCreateRequest request, Member currentUser) {
        if (postId == null)
            throw new IllegalArgumentException("Post ID cannot be null");
        // postId로 게시글을 찾음
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Comment comment = new Comment();
        comment.setNickname(currentUser.getNickname());
        comment.setContent(request.getContent());
        comment.setSecret(request.isSecret()); // 비밀 댓글 여부 설정
        comment.setMember(currentUser); // 실제 작성자 설정
        comment.setPost(post); // 게시글에 댓글 연결

        // 대댓글인 경우 부모 댓글 설정
        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));
            if (!parent.getPost().getId().equals(postId)) {
                throw new IllegalArgumentException("부모 댓글이 해당 게시글에 존재하지 않습니다.");
            }
            comment.setParent(parent);
        }

        commentRepository.save(comment);

        // 게시글의 댓글 수 증가
        post.incrementCommentCount();
        postRepository.save(post);
    }

    // 3. 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Member currentUser) {
        if (commentId == null)
            throw new IllegalArgumentException("Comment ID cannot be null");
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
        if (commentId == null)
            throw new IllegalArgumentException("Comment ID cannot be null");
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

    // 5. 내가 작성한 댓글 조회 (프로필용)
    public List<com.example.anonymous_board.dto.MyCommentResponse> getMyComments(Member member) {
        List<Comment> comments = commentRepository.findByMemberOrderByCreatedAtDesc(member);
        return comments.stream()
                .map(comment -> com.example.anonymous_board.dto.MyCommentResponse.builder()
                        .id(comment.getId())
                        .postId(comment.getPost().getId())
                        .postTitle(comment.getPost().getTitle())
                        .content(comment.getContent().length() > 100
                                ? comment.getContent().substring(0, 100) + "..."
                                : comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // 6. 내가 작성한 댓글 조회 (페이지네이션)
    public org.springframework.data.domain.Page<com.example.anonymous_board.dto.MyCommentResponse> getMyCommentsPaged(
            Member member, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by("createdAt").descending());
        org.springframework.data.domain.Page<Comment> commentPage = commentRepository.findByMember(member, pageable);

        return commentPage.map(comment -> com.example.anonymous_board.dto.MyCommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .postTitle(comment.getPost().getTitle())
                .content(comment.getContent().length() > 100
                        ? comment.getContent().substring(0, 100) + "..."
                        : comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build());
    }
}
