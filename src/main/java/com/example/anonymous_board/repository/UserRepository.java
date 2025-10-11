package com.example.anonymous_board.repository;

import com.example.anonymous_board.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByNicknameAndProvider(String nickname, String provider);
}
