package com.example.anonymous_board.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    GUEST("ROLE_GUEST", "손님"), // 비회원
    USER("ROLE_USER", "일반 사용자"), // 회원
    ADMIN("ROLE_ADMIN", "관리자"); // 관리자

    private final String key; // 권한 키
    private final String title; // 권한 제목
}
