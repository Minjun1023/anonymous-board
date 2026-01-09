package com.example.anonymous_board.controllers.api;

import com.example.anonymous_board.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// 관리자 페이지
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 게시글의 실제 작성자 정보 조회 (관리자 전용)
    @GetMapping("/posts/{postId}/author")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPostAuthor(@PathVariable Long postId) {
        try {
            return ResponseEntity.ok(adminService.getPostAuthorInfo(postId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 댓글의 실제 작성자 정보 조회 (관리자 전용)
    @GetMapping("/comments/{commentId}/author")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCommentAuthor(@PathVariable Long commentId) {
        try {
            return ResponseEntity.ok(adminService.getCommentAuthorInfo(commentId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 모든 게시글 목록 (작성자 정보 포함)
    // 한 페이지에 가져오는 게시글 수 20개
    @GetMapping("/posts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllPostsWithAuthors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllPostsWithAuthors(page, size));
    }

    // 특정 사용자의 모든 게시글 조회
    @GetMapping("/users/{userId}/posts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserPosts(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(adminService.getUserPosts(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 특정 사용자의 모든 댓글 조회
    @GetMapping("/users/{userId}/comments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserComments(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(adminService.getUserComments(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 모든 사용자 목록 조회
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllUsers(page, size));
    }
}
