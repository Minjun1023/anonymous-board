package com.example.anonymous_board.service;

import com.example.anonymous_board.dto.PostCreateRequest;
import com.example.anonymous_board.dto.PostUpdateRequest;
import com.example.anonymous_board.entity.Post;
import com.example.anonymous_board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    // 1. 게시글 생성
    public void createPost(PostCreateRequest request) {
        Post post = new Post();
        post.setNickname(request.getNickname());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setPassword(request.getPassword());
        postRepository.save(post);
    }

    // 2. 게시글 전체 조회
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    // 3. 게시글 단건 조회
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    // 4. 게시글 수정
    @Transactional
    public void updatePost(Long id, PostUpdateRequest request) {
        Post post = getPostById(id);

        // 비밀번호 확인
        if (!post.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
    }

    // 5. 게시글 삭제
    public void deletePost(Long id, String password) {
        Post post = getPostById(id);

        // 비밀번호 확인
        if (!post.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        postRepository.delete(post);
    }
}
