package com.example.anonymous_board.repository;

import com.example.anonymous_board.domain.EmailVerificationToken;
import com.example.anonymous_board.domain.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByEmail(String email);

    Optional<EmailVerificationToken> findByEmailAndTokenType(String email, TokenType tokenType);

    Optional<EmailVerificationToken> findByToken(String token);
}
