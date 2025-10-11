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

    private String email; // 인증할 이메일 주소를 직접 저장

    @Column(unique = true)
    private String token; // 인증 토큰

    private LocalDateTime expirationTime; // 만료 시간

    private boolean expired; // 만료 여부

    private boolean verified; // 사용 완료 여부

    public static EmailVerificationToken create(String email, String token) {
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.email = email;
        verificationToken.token = token;
        verificationToken.expirationTime = LocalDateTime.now().plusMinutes(EMAIL_TOKEN_EXPIRATION_MINUTES);
        verificationToken.expired = false;
        verificationToken.verified = false;
        return verificationToken;
    }

    public void setExpired() {
        this.expired = true;
    }
    
    public void setVerified() {
        this.verified = true;
    }
}
