package com.example.anonymous_board.controllers.api;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.dto.NewPasswordRequest;
import com.example.anonymous_board.dto.ResetPasswordRequest;
import com.example.anonymous_board.dto.TokenInfo;
import com.example.anonymous_board.dto.UserLoginRequest;
import com.example.anonymous_board.dto.UserSignupRequest;
import com.example.anonymous_board.service.EmailService;
import com.example.anonymous_board.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody UserSignupRequest request) {
        userService.signup(request);
        return ResponseEntity.ok("회원가입이 성공적으로 완료되었습니다.");
    }

    // 아이디 중복 확인
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        return ResponseEntity.ok(Collections.singletonMap("exists", userService.isUsernameExist(username)));
    }

    // 이메일 중복 확인
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(Collections.singletonMap("exists", userService.isEmailExist(email)));
    }

    // 닉네임 중복 확인
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(Collections.singletonMap("exists", userService.isNicknameExist(nickname)));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody UserLoginRequest request,
            HttpServletResponse response) {
        try {
            TokenInfo tokenInfo = userService.login(request.getUsername(), request.getPassword());

            // HttpOnly 쿠키 (보안용 - XSS 방지)
            Cookie httpOnlyCookie = new Cookie("access_token", tokenInfo.getAccessToken());
            httpOnlyCookie.setPath("/");
            httpOnlyCookie.setHttpOnly(true);
            httpOnlyCookie.setMaxAge(60 * 60 * 24); // 24시간
            httpOnlyCookie.setSecure(false); // 개발 환경에서는 false, 프로덕션에서는 true
            response.addCookie(httpOnlyCookie);

            // JavaScript 접근 가능한 쿠키 (UI 업데이트용)
            Cookie jsCookie = new Cookie("jwt_token", tokenInfo.getAccessToken());
            jsCookie.setPath("/");
            jsCookie.setHttpOnly(false);
            jsCookie.setMaxAge(60 * 60 * 24); // 24시간
            jsCookie.setSecure(false); // 개발 환경에서는 false, 프로덕션에서는 true
            response.addCookie(jsCookie);

            Map<String, String> result = new HashMap<>();
            result.put("message", "로그인 성공");
            result.put("redirectUrl", "/");
            result.put("token", tokenInfo.getAccessToken()); // 응답에도 토큰 포함
            return ResponseEntity.ok(result);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // 아이디 찾기
    @GetMapping("/find-id")
    public ResponseEntity<Map<String, String>> findId(@RequestParam String email) {
        try {
            String username = userService.findUsernameByEmail(email);
            return ResponseEntity.ok(Collections.singletonMap("username", username));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    // 비밀번호 재설정 링크 발송
    @PostMapping("/reset-password")
    public ResponseEntity<String> sendResetPasswordEmail(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            Member member = userService.findByUsername(request.getUsername());
            emailService.sendPasswordResetLink(member);
            return ResponseEntity.ok("비밀번호 재설정 링크가 이메일로 발송되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
        }
    }

    // 비밀번호 재설정
    @PostMapping("/reset-password-confirm")
    public ResponseEntity<Map<String, String>> resetPasswordConfirm(@Valid @RequestBody NewPasswordRequest request) {
        try {
            userService.resetPassword(request.getToken(), request.getPassword());
            return ResponseEntity.ok(Collections.singletonMap("message", "비밀번호가 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    // 로그인된 사용자 정보
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Member member) {
        if (member == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        // 필요한 정보만 반환
        Map<String, Object> response = new HashMap<>();
        response.put("id", member.getId());
        response.put("username", member.getUsername());
        response.put("email", member.getEmail());
        response.put("nickname", member.getNickname());
        response.put("provider", member.getProvider());
        response.put("role", member.getRole().getKey()); // ROLE_ADMIN 형식으로 반환

        return ResponseEntity.ok(response);
    }
}
