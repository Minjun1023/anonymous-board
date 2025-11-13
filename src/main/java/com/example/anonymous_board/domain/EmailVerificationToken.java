package com.example.anonymous_board.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationToken {

    private static final long EMAIL_TOKEN_EXPIRATION_MINUTES = 5L; // 토큰 만료 시간: 5분

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long id;

    @Column(nullable = false)
    private String email; // 인증할 이메일 주소를 직접 저장

    @Column(nullable = false)
    private String token; // 인증 토큰

    @Enumerated(EnumType.STRING)
    private TokenType tokenType; // 토큰 타입

    private LocalDateTime expirationTime; // 만료 시간

    private LocalDateTime createdAt; // 생성 시간

    private boolean expired; // 만료 여부

    private boolean verified; // 사용 완료 여부

    public static EmailVerificationToken create(String email, String token, TokenType tokenType) {
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.email = email;
        verificationToken.token = token;
        verificationToken.tokenType = tokenType;
        verificationToken.expirationTime = LocalDateTime.now().plusMinutes(EMAIL_TOKEN_EXPIRATION_MINUTES);
        verificationToken.createdAt = LocalDateTime.now();
        verificationToken.expired = false;
        verificationToken.verified = false;
        return verificationToken;
    }

    public void updateToken(String newToken) {
        this.token = newToken;
        this.expirationTime = LocalDateTime.now().plusMinutes(EMAIL_TOKEN_EXPIRATION_MINUTES);
        this.createdAt = LocalDateTime.now();
        this.expired = false;
        this.verified = false;
    }

    // 인증 링크 만료 시
    public void setExpired() {
        this.expired = true;
    }
    
    // 인증 완료 시
    public void setVerified() {
        this.verified = true;
    }
}
