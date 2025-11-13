package com.example.anonymous_board.service;

import com.example.anonymous_board.domain.*;
import com.example.anonymous_board.dto.PostCreateRequest;
import com.example.anonymous_board.dto.PostUpdateRequest;
import com.example.anonymous_board.repository.PostRepository;
import com.example.anonymous_board.repository.UserRepository;
import com.example.anonymous_board.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;

    // 1. 게시글 생성
    @Transactional
    public void createPost(PostCreateRequest request, Member currentUser) {
        Post post = new Post();
        post.setNickname(currentUser.getNickname());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setMember(currentUser);  // 실제 작성자 설정
        postRepository.save(post);
    }

    // 2. 게시글 전체 조회
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }
    
    // 2-1. 게시글 전체 조회 (정렬 및 페이지네이션)
    public Page<Post> getAllPosts(int page, int size, String sortBy) {
        Pageable pageable;
        
        switch (sortBy) {
            case "likes":
                pageable = PageRequest.of(page, size, Sort.by("likes").descending());
                break;
            case "dislikes":
                pageable = PageRequest.of(page, size, Sort.by("dislikes").descending());
                break;
            case "viewCount":
                pageable = PageRequest.of(page, size, Sort.by("viewCount").descending());
                break;
            case "comments":
                pageable = PageRequest.of(page, size, Sort.by("commentCount").descending());
                break;
            case "latest":
            default:
                pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                break;
        }
        
        return postRepository.findAll(pageable);
    }

    // 3. 게시글 단건 조회
    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    // 조회수 증가
    @Transactional
    public void incrementViewCount(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        post.incrementViewCount();
    }

    // 4. 게시글 수정
    @Transactional
    public void updatePost(Long id, PostUpdateRequest request, Member currentUser) {
        Post post = getPostById(id);

        // 본인 확인 (작성자만 수정 가능)
        if (!post.getMember().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
    }

    // 5. 게시글 삭제
    @Transactional
    public void deletePost(Long id, Member currentUser) {
        Post post = getPostById(id);

        // 본인 확인 (작성자만 삭제 가능)
        if (!post.getMember().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        postRepository.delete(post);
    }

    // 6. 추천/비추천
    @Transactional
    public void vote(Long postId, Member currentUser, VoteType voteType) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (post.getMember().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("자신의 글에는 추천/비추천할 수 없습니다.");
        }

        Optional<Vote> existingVote = voteRepository.findByMemberAndPost(currentUser, post);

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            // 이미 같은 타입으로 투표했다면, 투표 취소
            if (vote.getVoteType() == voteType) {
                if (voteType == VoteType.LIKE) {
                    post.decrementLikes();
                } else {
                    post.decrementDislikes();
                }
                voteRepository.delete(vote);
            } else { // 다른 타입으로 투표했다면, 투표 변경
                if (voteType == VoteType.LIKE) {
                    post.decrementDislikes();
                    post.incrementLikes();
                } else {
                    post.incrementDislikes();
                    post.decrementLikes();
                }
                vote.setVoteType(voteType);
                voteRepository.save(vote);
            }
        } else { // 첫 투표
            if (voteType == VoteType.LIKE) {
                post.incrementLikes();
            } else {
                post.incrementDislikes();
            }
            voteRepository.save(new Vote(currentUser, post, voteType));
        }
    }

    // 7. 게시글 검색 (대소문자 구분 없음, 페이지네이션 지원)
    public Page<Post> searchPosts(String keyword, int page, int size) {
        String lowerKeyword = keyword.toLowerCase();
        List<Post> allPosts = postRepository.findAll();
        
        List<Post> filteredPosts = allPosts.stream()
                .filter(post -> 
                    post.getTitle().toLowerCase().contains(lowerKeyword) || 
                    post.getContent().toLowerCase().contains(lowerKeyword)
                )
                .collect(java.util.stream.Collectors.toList());
        
        // 페이지네이션 처리
        int start = page * size;
        int end = Math.min(start + size, filteredPosts.size());
        
        if (start > filteredPosts.size()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), filteredPosts.size());
        }
        
        List<Post> pagedPosts = filteredPosts.subList(start, end);
        return new PageImpl<>(pagedPosts, PageRequest.of(page, size), filteredPosts.size());
    }

    public List<Post> getPostsByMember(Member member) {
        return postRepository.findByMemberOrderByCreatedAtDesc(member);
    }
}

