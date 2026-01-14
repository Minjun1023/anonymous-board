package com.example.anonymous_board.auth.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        // 예외 메시지를 URL 파라미터로 인코딩하여 전달
        String errorMessage = exception.getMessage();
        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);

        // 로그인 페이지로 리다이렉트하면서 에러 메시지 전달
        String targetUrl = "/login?error=true&message=" + encodedMessage;

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
