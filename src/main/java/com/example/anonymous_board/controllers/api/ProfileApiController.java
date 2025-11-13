package com.example.anonymous_board.controllers.api;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileApiController {

    private final UserService userService;
    private final String UPLOAD_DIR = "uploads/profiles/";  // 이미지 저장 경로
    
    // 프로필 이미지 업데이트
    @PostMapping("/image")
    public ResponseEntity<String> updateProfileImage(
            @AuthenticationPrincipal UserDetails userDetails,
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
            userService.updateProfileImage(userDetails.getUsername(), imageUrl);

            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("파일 업로드에 실패했습니다.");
        }
    }

    // 프로필 이미지 가져오기
    @GetMapping("/image")
    public ResponseEntity<String> getProfileImage(@AuthenticationPrincipal UserDetails userDetails) {
        // 현재 로그인한 사용자의 이메일(또는 username)로 DB에서 Member 조회
        Member member = userService.findByEmail(userDetails.getUsername());

        if (member != null && member.getProfileImage() != null) {
            // DB에 저장된 사용자 프로필 이미지 경로 반환
            return ResponseEntity.ok(member.getProfileImage());
        } else {
            // 프로필 이미지가 없는 경우 기본 이미지 반환
            return ResponseEntity.ok("/profiles/default_profile.png");
        }
    }
}