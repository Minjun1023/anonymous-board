package com.example.anonymous_board.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(unique = true)
    private String nickname;

    private String provider;

    private boolean emailVerified;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder
    public User(String email, String password, String nickname, String provider, boolean emailVerified, Role role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.provider = provider;
        this.emailVerified = emailVerified;
        this.role = role;
    }

    public User updateNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }
}
