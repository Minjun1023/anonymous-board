package com.example.anonymous_board.controller;

import com.example.anonymous_board.dto.TokenInfo;
import com.example.anonymous_board.dto.UserLoginRequest;
import com.example.anonymous_board.dto.UserSignupRequest;
import com.example.anonymous_board.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody UserSignupRequest request) {
        Long userId = userService.signup(request);
        log.info("New user created: id={}, email={}", userId, request.getEmail());
        return ResponseEntity.ok("회원가입이 성공적으로 완료되었습니다. 이메일 인증을 진행해주세요.");
    }

    @PostMapping("/login")
    public ResponseEntity<TokenInfo> login(@Valid @RequestBody UserLoginRequest request) {
        TokenInfo tokenInfo = userService.login(request.getEmail(), request.getPassword());
        log.info("User logged in: email={}", request.getEmail());
        return ResponseEntity.ok(tokenInfo);
    }
}
