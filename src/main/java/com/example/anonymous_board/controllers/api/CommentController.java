package com.example.anonymous_board.controllers.api;

import com.example.anonymous_board.domain.Member;

import com.example.anonymous_board.dto.CommentCreateRequest;
import com.example.anonymous_board.dto.CommentUpdateRequest;
import com.example.anonymous_board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 1. 댓글 목록 조회 API
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<?> getComments(@PathVariable("postId") Long postId,
            @AuthenticationPrincipal Member currentUser) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId, currentUser));
    }

    // 2. 댓글 작성 API
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<String> createComment(@PathVariable("postId") Long postId,
            @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal Member user) {
        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        commentService.createComment(postId, request, user);
        return ResponseEntity.ok("댓글이 성공적으로 작성되었습니다.");
    }

    // 3. 댓글 삭제 API
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal Member user) {
        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        try {
            commentService.deleteComment(commentId, user);
            return ResponseEntity.ok("댓글이 성공적으로 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. 댓글 수정 API
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<String> updateComment(@PathVariable("commentId") Long commentId,
            @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal Member user) {
        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        try {
            commentService.updateComment(commentId, request, user);
            return ResponseEntity.ok("댓글 수정이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
