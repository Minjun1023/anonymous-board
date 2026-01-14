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

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isSuspended = false; // 정지 상태

    private LocalDateTime suspendedUntil; // 정지 해제 시간 (null이면 영구 정지)

    @Column(length = 500)
    private String suspensionReason; // 정지 사유

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

    // 사용자 정지
    public void suspend(LocalDateTime until, String reason) {
        this.isSuspended = true;
        this.suspendedUntil = until; // null이면 영구 정지
        this.suspensionReason = reason;
    }

    // 정지 해제
    public void unsuspend() {
        this.isSuspended = false;
        this.suspendedUntil = null;
        this.suspensionReason = null;
    }

    // 현재 정지 상태 확인 (만료된 정지는 자동으로 해제되지 않음)
    public boolean isCurrentlySuspended() {
        if (!this.isSuspended) {
            return false;
        }
        // 영구 정지인 경우
        if (this.suspendedUntil == null) {
            return true;
        }
        // 임시 정지인 경우 - 만료 여부 확인
        return LocalDateTime.now().isBefore(this.suspendedUntil);
    }
}
