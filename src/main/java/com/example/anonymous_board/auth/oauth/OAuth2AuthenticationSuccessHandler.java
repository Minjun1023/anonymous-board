package com.example.anonymous_board.auth.oauth;

import com.example.anonymous_board.config.jwt.JwtTokenProvider;
import com.example.anonymous_board.dto.TokenInfo;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            // 인증 정보를 기반으로 JWT 토큰 생성
            TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

            // 리다이렉트 될 URL 생성
            String targetUrl = UriComponentsBuilder.fromUriString("/auth_success.html")
                    .queryParam("accessToken", tokenInfo.getAccessToken())
                    .queryParam("refreshToken", tokenInfo.getRefreshToken())
                    .build().toUriString();

            // 로그 추가
            log.info("OAuth2 Login Successful, Redirecting to: {}", targetUrl);
            log.info("Authentication: {}", authentication.getName());

            // 생성된 URL로 리다이렉트
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } catch (Exception e) {
            log.error("OAuth2 Authentication Success Handler Error", e);
            response.sendRedirect("/login?error=oauth2");
        }
    }
}
