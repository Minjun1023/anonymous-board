package com.example.anonymous_board.controllers.api;

import com.example.anonymous_board.dto.SuspendUserRequest;
import com.example.anonymous_board.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {

    private final UserService userService;

    // 사용자 정지
    @PostMapping("/{userId}/suspend")
    public ResponseEntity<Map<String, String>> suspendUser(
            @PathVariable Long userId,
            @RequestBody SuspendUserRequest request) {

        try {
            userService.suspendUser(userId, request.getSuspendDays(), request.getReason());
            return ResponseEntity.ok(Map.of("message", "사용자가 정지되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 사용자 정지 해제
    @PostMapping("/{userId}/unsuspend")
    public ResponseEntity<Map<String, String>> unsuspendUser(@PathVariable Long userId) {
        try {
            userService.unsuspendUser(userId);
            return ResponseEntity.ok(Map.of("message", "사용자 정지가 해제되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
