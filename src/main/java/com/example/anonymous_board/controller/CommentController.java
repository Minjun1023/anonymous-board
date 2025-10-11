package com.example.anonymous_board.controller;

import com.example.anonymous_board.dto.CommentCreateRequest;
import com.example.anonymous_board.dto.CommentUpdateRequest;
import com.example.anonymous_board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 1. 댓글 작성 API
    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<String> createComment(@PathVariable Long postId, @RequestBody CommentCreateRequest request) {
        commentService.createComment(postId, request);
        return ResponseEntity.ok("댓글이 성공적으로 작성되었습니다.");
    }

    // 2. 댓글 삭제 API
    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId, @RequestParam String password) {
        try {
            commentService.deleteComment(commentId, password);
            return ResponseEntity.ok("댓글이 성공적으로 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. 댓글 수정 API
    @PutMapping("/api/comments/{commentId}")
    public ResponseEntity<String> updateComment(@PathVariable Long commentId, @RequestBody CommentUpdateRequest request) {
        try {
            commentService.updateComment(commentId, request);
            return ResponseEntity.ok("댓글 수정이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
