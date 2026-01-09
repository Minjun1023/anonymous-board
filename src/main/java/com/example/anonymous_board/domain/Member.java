package com.example.anonymous_board.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자를 protected로 만들어 외부에서 직접 호출하지 못하도록 막음
@Entity
@Table(name = "users")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id; // 사용자 번호 (가입 순서)

    @Column(unique = true)
    private String username; // 사용자 아이디

    @Column(unique = true)
    private String email; // 이메일

    private String password; // 비밀번호

    @Column(unique = true)
    private String nickname; // 닉네임

    private String provider; // 로그인 제공자

    private boolean emailVerified; // 이메일 인증 여부

    @Column(columnDefinition = "varchar(255) default '/profiles/default_profile.png'") // 프로필 이미지 파일명
    private String profileImage; // 프로필 이미지

    @Enumerated(EnumType.STRING)
    private Role role; // 사용자 권한

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt; // 회원 생성된 시간

    @Builder
    public Member(String username, String email, String password, String nickname, String provider,
            boolean emailVerified, Role role, String profileImage) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.provider = provider;
        this.emailVerified = emailVerified;
        this.role = role;
        this.profileImage = profileImage != null ? profileImage : "/profiles/default_profile.png";
    }

    // 닉네임 수정 시
    public Member updateNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    // 비밀번호 수정 시
    public void updatePassword(String password) {
        this.password = password;
    }

    // 이메일 인증 완료 시
    public void verifyEmail() {
        this.emailVerified = true;
    }

    // 새 프로필 이미지 업로드 시
    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
