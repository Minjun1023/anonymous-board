package com.example.anonymous_board.controller;

import com.example.anonymous_board.dto.PostCreateRequest;
import com.example.anonymous_board.dto.PostResponse;
import com.example.anonymous_board.dto.PostUpdateRequest;
import com.example.anonymous_board.entity.Post;
import com.example.anonymous_board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 1. 게시글 생성 API
    @PostMapping("/api/posts")
    public ResponseEntity<String> createPost(@RequestBody PostCreateRequest request) {
        postService.createPost(request);
        return ResponseEntity.ok("게시글이 성공적으로 작성되었습니다.");
    }

    // 2. 게시글 전체 조회 API
    @GetMapping("/api/posts")
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        List<PostResponse> posts = postService.getAllPosts().stream()
                .map(PostResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(posts);
    }

    // 3. 게시글 단건 조회 API
    @GetMapping("/api/posts/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        Post post = postService.getPostById(id);
        return ResponseEntity.ok(new PostResponse(post));
    }

    // 4. 게시글 수정 API
    @PutMapping("/api/posts/{id}")
    public ResponseEntity<String> updatePost(@PathVariable Long id, @RequestBody PostUpdateRequest request) {
        try {
            postService.updatePost(id, request);
            return ResponseEntity.ok("게시글이 성공적으로 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 5. 게시글 삭제 API
    @DeleteMapping("/api/posts/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Long id, @RequestParam String password) {
        try {
            postService.deletePost(id, password);
            return ResponseEntity.ok("게시글이 성공적으로 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
