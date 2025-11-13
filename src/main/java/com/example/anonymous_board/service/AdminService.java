package com.example.anonymous_board.service;

import com.example.anonymous_board.domain.Comment;
import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Post;
import com.example.anonymous_board.dto.*;
import com.example.anonymous_board.repository.CommentRepository;
import com.example.anonymous_board.repository.PostRepository;
import com.example.anonymous_board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    // 게시글 작성자 정보 조회
    public AuthorInfoResponse getPostAuthorInfo(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        
        if (post.getMember() == null) {
            throw new IllegalArgumentException("이 게시글은 구버전 데이터로 작성자 정보가 없습니다.");
        }

        Member author = post.getMember();
        return AuthorInfoResponse.builder()
                .userId(author.getId())
                .email(author.getEmail())
                .name(author.getNickname())
                .provider(author.getProvider())
                .role(author.getRole().getTitle())
                .createdAt(post.getCreatedAt())
                .build();
    }

    // 댓글 작성자 정보 조회
    public AuthorInfoResponse getCommentAuthorInfo(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        
        if (comment.getMember() == null) {
            throw new IllegalArgumentException("이 댓글은 구버전 데이터로 작성자 정보가 없습니다.");
        }

        Member author = comment.getMember();
        return AuthorInfoResponse.builder()
                .userId(author.getId())
                .email(author.getEmail())
                .name(author.getNickname())
                .provider(author.getProvider())
                .role(author.getRole().getTitle())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    // 모든 게시글 (작성자 정보 포함)
    public Page<AdminPostResponse> getAllPostsWithAuthors(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postRepository.findAll(pageable);
        
        return posts.map(post -> {
            AuthorInfoResponse authorInfo = null;
            if (post.getMember() != null) {
                Member author = post.getMember();
                authorInfo = AuthorInfoResponse.builder()
                        .userId(author.getId())
                        .email(author.getEmail())
                        .name(author.getNickname())
                        .provider(author.getProvider())
                        .role(author.getRole().getTitle())
                        .createdAt(post.getCreatedAt())
                        .build();
            }
            
            return AdminPostResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .nickname(post.getNickname())
                    .content(post.getContent())
                    .viewCount(post.getViewCount())
                    .createdAt(post.getCreatedAt())
                    .authorInfo(authorInfo)
                    .build();
        });
    }

    // 특정 사용자의 게시글 목록
    public List<AdminPostResponse> getUserPosts(Long userId) {
        Member user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        List<Post> posts = postRepository.findByMemberOrderByCreatedAtDesc(user);
        
        AuthorInfoResponse authorInfo = AuthorInfoResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getNickname())
                .provider(user.getProvider())
                .role(user.getRole().getTitle())
                .build();
        
        return posts.stream()
                .map(post -> AdminPostResponse.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .nickname(post.getNickname())
                        .content(post.getContent())
                        .viewCount(post.getViewCount())
                        .createdAt(post.getCreatedAt())
                        .authorInfo(authorInfo)
                        .build())
                .collect(Collectors.toList());
    }

    // 특정 사용자의 댓글 목록
    public List<AdminCommentResponse> getUserComments(Long userId) {
        Member user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        List<Comment> comments = commentRepository.findByMemberOrderByCreatedAtDesc(user);
        
        AuthorInfoResponse authorInfo = AuthorInfoResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getNickname())
                .provider(user.getProvider())
                .role(user.getRole().getTitle())
                .build();
        
        return comments.stream()
                .map(comment -> AdminCommentResponse.builder()
                        .id(comment.getId())
                        .postId(comment.getPost().getId())
                        .postTitle(comment.getPost().getTitle())
                        .nickname(comment.getNickname())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .authorInfo(authorInfo)
                        .build())
                .collect(Collectors.toList());
    }

    // 모든 사용자 목록
    public Page<UserInfoResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Member> users = userRepository.findAll(pageable);
        
        return users.map(user -> {
            int postCount = postRepository.countByMember(user);
            int commentCount = commentRepository.countByMember(user);
            
            return UserInfoResponse.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getNickname())
                    .provider(user.getProvider())
                    .role(user.getRole().getTitle())
                    .postCount(postCount)
                    .commentCount(commentCount)
                    .createdAt(user.getCreatedAt())
                    .build();
        });
    }
}
