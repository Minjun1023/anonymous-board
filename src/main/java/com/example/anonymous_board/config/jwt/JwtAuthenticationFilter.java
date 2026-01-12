package com.example.anonymous_board.config.jwt;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.repository.UserRepository;
import com.example.anonymous_board.service.JwtBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final JwtBlacklistService jwtBlacklistService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = jwtTokenProvider.resolveToken(httpRequest); // 공통 메서드 사용

        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 블랙리스트 확인 (로그아웃된 토큰인지)
            if (jwtBlacklistService.isBlacklisted(token)) {
                log.debug("블랙리스트에 등록된 JWT 토큰");
                chain.doFilter(request, response);
                return;
            }

            String email = jwtTokenProvider.getEmail(token);
            Member member = userRepository.findByEmail(email).orElse(null);

            if (member != null) {
                // Role.getKey()는 이미 "ROLE_ADMIN" 형식을 반환함
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(member.getRole().getKey());

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        member,
                        null,
                        Collections.singletonList(authority));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }
}
