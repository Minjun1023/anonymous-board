package com.example.anonymous_board.repository;

import com.example.anonymous_board.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email); // 이메일로 사용자 조회

    Optional<Member> findByUsername(String username); // 사용자 이름으로 사용자 조회

    boolean existsByEmail(String email); // 이메일로 사용자 존재 여부 조회

    boolean existsByUsername(String username); // 사용자 이름으로 사용자 존재 여부 조회

    boolean existsByNickname(String nickname); // 닉네임으로 사용자 존재 여부 조회

    Optional<Member> findByNickname(String nickname); // 닉네임으로 사용자 조회

    Optional<Member> findByNicknameAndProvider(String nickname, String provider); // 닉네임과 프로바이더로 사용자 조회
}
