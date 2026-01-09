package com.example.anonymous_board.controllers.api;

import com.example.anonymous_board.dto.EmailAuthRequest;
import com.example.anonymous_board.dto.EmailCheckRequest;
import com.example.anonymous_board.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    /**
     * 인증 이메일 발송 요청
     */
    @PostMapping("/send-verification")
    public ResponseEntity<String> sendVerificationEmail(@Valid @RequestBody EmailAuthRequest request) {
        emailService.sendVerificationEmail(request.getEmail());
        return ResponseEntity.ok("인증 이메일이 성공적으로 발송되었습니다. 이메일을 확인해주세요.");
    }

    /**
     * 이메일로 받은 인증 코드 확인
     */
    @PostMapping("/check-verification")
    public ResponseEntity<String> checkVerification(@Valid @RequestBody EmailCheckRequest request) {
        emailService.verifyEmail(request);
        return ResponseEntity.ok("이메일 인증이 성공적으로 완료되었습니다.");
    }
}
