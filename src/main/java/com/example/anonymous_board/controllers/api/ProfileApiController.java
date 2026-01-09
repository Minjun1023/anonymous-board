package com.example.anonymous_board.controllers.api;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.dto.MyPostResponse;
import com.example.anonymous_board.dto.ProfileNicknameUpdateRequest;
import com.example.anonymous_board.dto.ChangePasswordRequest;
import com.example.anonymous_board.dto.DeleteAccountRequest;
import com.example.anonymous_board.dto.MyCommentResponse;
import com.example.anonymous_board.service.PostService;
import com.example.anonymous_board.service.CommentService;
import com.example.anonymous_board.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.Optional;

import com.example.anonymous_board.repository.UserRepository;
import com.example.anonymous_board.dto.UserInfoResponse;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileApiController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PostService postService;
    private final CommentService commentService;
    private final String UPLOAD_DIR = "uploads/profiles/"; // 이미지 저장 경로

    // 내 프로필 정보 조회 (테스트 호환성 및 편의성)
    @GetMapping
    public ResponseEntity<UserInfoResponse> getProfile(
            @AuthenticationPrincipal Member member) {
        if (member == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(UserInfoResponse.of(member));
    }

    // 프로필 이미지 업데이트
    @PostMapping("/image")
    public ResponseEntity<String> updateProfileImage(
            @AuthenticationPrincipal Member member,
            @RequestParam("file") MultipartFile file) {
        try {
            // 업로드 디렉토리가 없으면 생성
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 파일 이름을 고유하게 생성
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // 파일 저장
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            // 프로필 이미지 URL 업데이트
            String imageUrl = "/profiles/" + newFilename;
            userService.updateProfileImage(member.getId(), imageUrl);

            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("파일 업로드에 실패했습니다.");
        }
    }

    // 닉네임 업데이트
    @PutMapping("/nickname")
    public ResponseEntity<String> updateNickname(
            @AuthenticationPrincipal Member member,
            @RequestBody ProfileNicknameUpdateRequest request) {
        try {
            userService.updateNickname(member, request.getNickname());
            return ResponseEntity.ok("닉네임이 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 프로필 이미지 가져오기
    @GetMapping("/image")
    public ResponseEntity<String> getProfileImage(@AuthenticationPrincipal Member member) {
        String imageUrl = "/profiles/default_profile.png";

        if (member != null) {
            // DB에서 최신 사용자 정보 조회 (캐싱된 정보 대신)
            Optional<Member> freshMember = userRepository.findById(member.getId());

            if (freshMember.isPresent()) {
                if (freshMember.get().getProfileImage() != null) {
                    imageUrl = freshMember.get().getProfileImage();
                }
            }
        }

        // 캐싱 방지 헤더 추가
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(imageUrl);
    }

    // 프로필 이미지 삭제
    @DeleteMapping("/image")
    public ResponseEntity<String> deleteProfileImage(@AuthenticationPrincipal Member member) {
        try {
            userService.updateProfileImage(member.getId(), null);
            return ResponseEntity.ok("/profiles/default_profile.png");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("프로필 이미지 삭제에 실패했습니다.");
        }
    }

    // 비밀번호 변경
    @PutMapping("/password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid ChangePasswordRequest request) {
        try {
            // 새 비밀번호와 확인 비밀번호 일치 검증
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body("새 비밀번호가 일치하지 않습니다.");
            }

            userService.changePassword(member, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 내가 작성한 게시글 조회 (페이지네이션)
    @GetMapping("/posts")
    public ResponseEntity<?> getMyPosts(
            @AuthenticationPrincipal Member member,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (member == null) {
            return ResponseEntity.status(401).build();
        }
        org.springframework.data.domain.Page<MyPostResponse> postPage = postService.getMyPostsPaged(member, page, size);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", postPage.getContent());
        response.put("currentPage", postPage.getNumber());
        response.put("totalPages", postPage.getTotalPages());
        response.put("totalElements", postPage.getTotalElements());
        response.put("hasNext", postPage.hasNext());
        response.put("hasPrevious", postPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    // 내가 작성한 댓글 조회 (페이지네이션)
    @GetMapping("/comments")
    public ResponseEntity<?> getMyComments(
            @AuthenticationPrincipal Member member,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (member == null) {
            return ResponseEntity.status(401).build();
        }
        org.springframework.data.domain.Page<MyCommentResponse> commentPage = commentService.getMyCommentsPaged(member,
                page, size);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", commentPage.getContent());
        response.put("currentPage", commentPage.getNumber());
        response.put("totalPages", commentPage.getTotalPages());
        response.put("totalElements", commentPage.getTotalElements());
        response.put("hasNext", commentPage.hasNext());
        response.put("hasPrevious", commentPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    // 회원 탈퇴
    @DeleteMapping("/account")
    public ResponseEntity<String> deleteAccount(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid DeleteAccountRequest request) {
        if (member == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        try {
            userService.deleteAccount(member, request.getPassword());
            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}