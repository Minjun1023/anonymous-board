package com.example.anonymous_board.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileImageUpdateRequest {
    private String imageUrl;  // 프로필 이미지 URL
}