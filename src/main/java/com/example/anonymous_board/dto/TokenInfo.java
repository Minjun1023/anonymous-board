package com.example.anonymous_board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenInfo {
    private String grantType; // 토큰 타입
    private String accessToken; // 액세스 토큰
    private String refreshToken; // 리프레시 토큰
}
