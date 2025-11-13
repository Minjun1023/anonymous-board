package com.example.anonymous_board.repository;

import com.example.anonymous_board.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

    Optional<Member> findByNickname(String nickname);

    Optional<Member> findByNicknameAndProvider(String nickname, String provider);
}
