package com.example.anonymous_board.auth.oauth;

import com.example.anonymous_board.config.jwt.JwtTokenProvider;
import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.dto.TokenInfo;
import com.example.anonymous_board.repository.UserRepository;
import com.example.anonymous_board.util.CookieUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static com.example.anonymous_board.auth.oauth.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final JwtTokenProvider jwtTokenProvider;    // JWT 토큰 생성기
    private final UserRepository userRepository;    // 사용자 정보 조회       
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;    // 쿠키 관리

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("응답이 이미 커밋되었습니다. 리다이렉션이 불가능 합니다. " + targetUrl);
            return;
        }

        // 로그인 과정 중 생성된 쿠키와 인증 속성 삭제
        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    // 로그인 완료 이후 어디로 이동할지 결정 및 JWT 토큰 발급
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 리다이렉트 URI 가져오기
        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);
        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        // 로그인한 사용자 정보 가져오기
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = getEmailFromOAuth2User(oAuth2User);

        // OAuth2 인증을 통해 받은 이메일로 DB에 저장된 유저 조회
        Member user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 주소를 가진 사용자를 찾을 수 없습니다: " + email));

        // 권한(Role)을 SecurityContext에 다시 세팅할 때 사용
        Authentication newAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                user,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getKey()))
        );

        // JWT 토큰 발급 및 쿠키에 저장
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(newAuth);
        
        // HttpOnly 쿠키 (보안용)
        Cookie httpOnlyCookie = new Cookie("access_token", tokenInfo.getAccessToken());
        httpOnlyCookie.setPath("/");
        httpOnlyCookie.setHttpOnly(true);
        httpOnlyCookie.setMaxAge(60 * 60 * 24); // 24시간
        httpOnlyCookie.setSecure(false);
        response.addCookie(httpOnlyCookie);
        
        // JavaScript 접근 가능한 쿠키 (UI 업데이트용)
        Cookie jsCookie = new Cookie("jwt_token", tokenInfo.getAccessToken());
        jsCookie.setPath("/");
        jsCookie.setHttpOnly(false);
        jsCookie.setMaxAge(60 * 60 * 24); // 24시간
        jsCookie.setSecure(false);
        response.addCookie(jsCookie);

        // 리다이렉트할 URL 반환
        return UriComponentsBuilder.fromUriString(targetUrl)
                .build().toUriString();
    }
    // 소셜 플랫폼마다 이메일 방식을 가져오는 방식을 구하기 위함
    private String getEmailFromOAuth2User(OAuth2User oAuth2User) {
        // Google
        if (oAuth2User.getAttributes().containsKey("email")) {
            return (String) oAuth2User.getAttributes().get("email");
            // Naver
        } else if (oAuth2User.getAttributes().containsKey("response")) {
            Object response = oAuth2User.getAttributes().get("response");
            if (response instanceof java.util.Map) {
                return (String) ((java.util.Map<?, ?>) response).get("email");
            }
            // Kakao
        } else if (oAuth2User.getAttributes().containsKey("kakao_account")) {
            Object kakaoAccount = oAuth2User.getAttributes().get("kakao_account");
            if (kakaoAccount instanceof java.util.Map) {
                return (String) ((java.util.Map<?, ?>) kakaoAccount).get("email");
            }
        }
        return null;
    }

    // 로그인 과정에서 생성된 OAuth2 관련 쿠키 삭제
    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}
