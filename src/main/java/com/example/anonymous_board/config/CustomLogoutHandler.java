package com.example.anonymous_board.config;

import com.example.anonymous_board.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

/**
 * 로그아웃 시 JWT 쿠키 삭제
 */
@Component
public class CustomLogoutHandler implements LogoutHandler {

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 모든 인증 관련 쿠키 삭제
        CookieUtils.deleteCookie(request, response, "access_token");
        CookieUtils.deleteCookie(request, response, "jwt_token");
        CookieUtils.deleteCookie(request, response, "refreshToken");
        CookieUtils.deleteCookie(request, response, "JSESSIONID");
    }
}
