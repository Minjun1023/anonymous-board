package com.example.anonymous_board.config.jwt;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    // 요청마다 실행되는 필터 로직
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = resolveToken(httpRequest);
        String requestURI = httpRequest.getRequestURI();
        log.info("Request URI: {}, Token present: {}", requestURI, token != null);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getEmail(token);
            log.info("Valid token for email: {}", email);
            Member member = userRepository.findByEmail(email).orElse(null);
            
            if (member != null) {
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(member.getRole().name());

                Authentication authentication = new UsernamePasswordAuthenticationToken (
                    member, // principal
                    null,   // credentials
                    Collections.singletonList(authority)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Authentication set for user: {}", email);
            } else {
                log.warn("Member not found for email: {}", email);
            }
        } else {
            log.warn("No valid token found for request: {}", requestURI);
        }
        chain.doFilter(request, response);
    }

    // 토큰 추출
    private String resolveToken(HttpServletRequest request) {
        // 1. Authorization 헤더 확인
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            log.info("Token found in Authorization header");
            return bearerToken.substring(7);
        }

        // 2. 쿠키에서 토큰 확인 (access_token 또는 jwt_token)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            log.info("Checking {} cookies", cookies.length);
            for (Cookie cookie : cookies) {
                log.info("Cookie: {} = {}", cookie.getName(), cookie.getValue().substring(0, Math.min(20, cookie.getValue().length())) + "...");
                if ("access_token".equals(cookie.getName()) || "jwt_token".equals(cookie.getName())) {
                    log.info("Token found in cookie: {}", cookie.getName());
                    return cookie.getValue();
                }
            }
        } else {
            log.warn("No cookies found in request");
        }
        return null;
    }
}
