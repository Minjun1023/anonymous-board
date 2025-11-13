package com.example.anonymous_board.controllers.api;
import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Post;
import com.example.anonymous_board.dto.PostCreateRequest;
import com.example.anonymous_board.dto.PostResponse;
import com.example.anonymous_board.dto.PostUpdateRequest;
import com.example.anonymous_board.dto.VoteRequest;
import com.example.anonymous_board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 1. 게시글 생성 API
    @PostMapping
    public ResponseEntity<String> createPost(@RequestBody PostCreateRequest request,
                                            @AuthenticationPrincipal Member user) {
        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        postService.createPost(request, user);
        return ResponseEntity.ok("게시글이 성공적으로 작성되었습니다.");
    }

    // 2. 게시글 전체 조회 API (페이지네이션 및 정렬)
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortBy,
            @AuthenticationPrincipal Member currentUser) {
        
        Page<Post> postPage = postService.getAllPosts(page, size, sortBy);
        
        List<PostResponse> posts = postPage.getContent().stream()
                .map(post -> new PostResponse(post, currentUser))
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("posts", posts);
        response.put("currentPage", postPage.getNumber());
        response.put("totalPages", postPage.getTotalPages());
        response.put("totalElements", postPage.getTotalElements());
        response.put("hasNext", postPage.hasNext());
        response.put("hasPrevious", postPage.hasPrevious());
        
        return ResponseEntity.ok(response);
    }

    // 7. 게시글 검색 API (/{id}보다 먼저 선언해야 함)
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Member currentUser) {
        
        Page<Post> postPage = postService.searchPosts(keyword, page, size);
        
        List<PostResponse> posts = postPage.getContent().stream()
                .map(post -> new PostResponse(post, currentUser))
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", posts);
        response.put("currentPage", postPage.getNumber());
        response.put("totalPages", postPage.getTotalPages());
        response.put("totalElements", postPage.getTotalElements());
        response.put("hasNext", postPage.hasNext());
        response.put("hasPrevious", postPage.hasPrevious());
        response.put("first", postPage.isFirst());
        response.put("last", postPage.isLast());
        response.put("number", postPage.getNumber());
        
        return ResponseEntity.ok(response);
    }

    // 3. 게시글 단건 조회 API
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable("id") Long id, @AuthenticationPrincipal Member currentUser) {
        postService.incrementViewCount(id);
        Post post = postService.getPostById(id);
        return ResponseEntity.ok(new PostResponse(post, currentUser));
    }

    // 4. 게시글 수정 API
    @PutMapping("/{id}")
    public ResponseEntity<String> updatePost(@PathVariable("id") Long id,
                                            @RequestBody PostUpdateRequest request,
                                            @AuthenticationPrincipal Member user) {
        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        try {
            postService.updatePost(id, request, user);
            return ResponseEntity.ok("게시글이 수정되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 5. 게시글 삭제 API
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@PathVariable("id") Long id,
                                            @AuthenticationPrincipal Member user) {
        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        try {
            postService.deletePost(id, user);
            return ResponseEntity.ok("게시글이 성공적으로 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 6. 추천/비추천 API
    @PostMapping("/{id}/vote")
    public ResponseEntity<String> votePost(@PathVariable("id") Long id,
                                           @RequestBody VoteRequest request,
                                           @AuthenticationPrincipal Member user) {
        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        try {
            postService.vote(id, user, request.getVoteType());
            return ResponseEntity.ok("투표가 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

