package com.example.anonymous_board.controllers.api;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Post;
import com.example.anonymous_board.dto.PostCreateRequest;
import com.example.anonymous_board.dto.PostResponse;
import com.example.anonymous_board.dto.PostUpdateRequest;
import com.example.anonymous_board.dto.VoteRequest;
import com.example.anonymous_board.service.PostService;
import com.example.anonymous_board.service.ViewCountService;
import com.example.anonymous_board.dto.PollVoteRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static java.util.Collections.singletonMap;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final ViewCountService viewCountService;

    // 1. 게시글 생성 API
    @PostMapping
    public ResponseEntity<Map<String, String>> createPost(@RequestPart("post") PostCreateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal Member user) {
        if (user == null) {
            return ResponseEntity.status(401).body(singletonMap("message", "로그인이 필요합니다."));
        }
        postService.createPost(request, files, user);
        return ResponseEntity.ok(singletonMap("message", "게시글이 성공적으로 작성되었습니다."));
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

    // 3. 게시글 검색 API (/{id}보다 먼저 선언해야 함)
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

    // 4. 핫 게시글 조회 API
    @GetMapping("/hot")
    public ResponseEntity<Map<String, Object>> getHotPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Member currentUser) {

        Page<Post> postPage = postService.getHotPosts(page, size);

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

    // 5. 게시글 단건 조회 API
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable("id") Long id,
            @AuthenticationPrincipal Member currentUser,
            HttpServletRequest request) {

        // 조회자 식별: 로그인 사용자는 userId, 비로그인은 IP
        String viewerIdentifier = currentUser != null
                ? "user:" + currentUser.getId()
                : "ip:" + getClientIp(request);

        // Redis에서 중복 조회 확인 후 조회수 증가
        if (viewCountService.canIncrementViewCount(id, viewerIdentifier)) {
            postService.incrementViewCount(id);
        }

        Post post = postService.getPostById(id);
        return ResponseEntity.ok(new PostResponse(post, currentUser));
    }

    // 클라이언트 IP 추출 (프록시 고려)
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    // 6. 게시글 수정 API
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updatePost(@PathVariable("id") Long id,
            @RequestPart("post") PostUpdateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal Member user) {
        if (user == null) {
            return ResponseEntity.status(401).body(singletonMap("message", "로그인이 필요합니다."));
        }
        try {
            postService.updatePost(id, request, files, user);
            return ResponseEntity.ok(singletonMap("message", "게시글이 수정되었습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(singletonMap("message", e.getMessage()));
        }
    }

    // 7. 게시글 삭제 API
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePost(@PathVariable("id") Long id,
            @AuthenticationPrincipal Member user) {
        if (user == null) {
            return ResponseEntity.status(401).body(singletonMap("message", "로그인이 필요합니다."));
        }
        try {
            postService.deletePost(id, user);
            return ResponseEntity.ok(singletonMap("message", "게시글이 성공적으로 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(singletonMap("message", e.getMessage()));
        }
    }

    // 8. 추천/비추천 API
    @PostMapping("/{id}/vote")
    public ResponseEntity<Map<String, String>> votePost(@PathVariable("id") Long id,
            @RequestBody VoteRequest request,
            @AuthenticationPrincipal Member user) {
        if (user == null) {
            return ResponseEntity.status(401).body(singletonMap("message", "로그인이 필요합니다."));
        }
        try {
            postService.vote(id, user, request.getVoteType());
            return ResponseEntity.ok(singletonMap("message", "투표가 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(singletonMap("message", e.getMessage()));
        }
    }

    // 9. 투표(설문) 참여 API
    @PostMapping("/{id}/poll/vote")
    public ResponseEntity<Map<String, String>> votePoll(@PathVariable("id") Long id,
            @RequestBody PollVoteRequest request,
            @AuthenticationPrincipal Member user) {
        if (user == null) {
            return ResponseEntity.status(401).body(singletonMap("message", "로그인이 필요합니다."));
        }
        try {
            postService.votePoll(request.getOptionId(), user);
            return ResponseEntity.ok(singletonMap("message", "투표가 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(singletonMap("message", e.getMessage()));
        }
    }
}
