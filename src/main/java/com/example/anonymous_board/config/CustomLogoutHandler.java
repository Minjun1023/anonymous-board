package com.example.anonymous_board.config;

import com.example.anonymous_board.config.jwt.JwtTokenProvider;
import com.example.anonymous_board.service.JwtBlacklistService;
import com.example.anonymous_board.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

/**
 * 로그아웃 시 JWT 쿠키 삭제 및 블랙리스트 추가
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final JwtBlacklistService jwtBlacklistService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // JWT 토큰 추출 및 블랙리스트에 추가 (공통 메서드 사용)
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null) {
            jwtBlacklistService.blacklistToken(token);
            log.info("로그아웃: JWT 블랙리스트에 추가됨");
        }

        // 모든 인증 관련 쿠키 삭제
        CookieUtils.deleteCookie(request, response, "access_token");
        CookieUtils.deleteCookie(request, response, "jwt_token");
        CookieUtils.deleteCookie(request, response, "refreshToken");
        CookieUtils.deleteCookie(request, response, "JSESSIONID");
    }
}
