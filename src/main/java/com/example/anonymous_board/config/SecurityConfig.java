package com.example.anonymous_board.config;

import com.example.anonymous_board.auth.oauth.CustomOAuth2UserService;
import com.example.anonymous_board.auth.oauth.OAuth2AuthenticationSuccessHandler;
import com.example.anonymous_board.auth.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.example.anonymous_board.auth.oauth.OAuth2AuthenticationFailureHandler;
import com.example.anonymous_board.config.jwt.JwtAuthenticationFilter;
import com.example.anonymous_board.config.jwt.JwtTokenProvider;
import com.example.anonymous_board.repository.UserRepository;
import com.example.anonymous_board.service.JwtBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 통합 관리 (JWT 인증 + OAuth2 로그인 + 로그아웃시 쿠키 삭제)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
        private final JwtTokenProvider jwtTokenProvider;
        private final UserRepository userRepository;
        private final CustomOAuth2UserService customOAuth2UserService;
        private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
        private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
        private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
        private final CustomLogoutHandler customLogoutHandler;
        private final JwtBlacklistService jwtBlacklistService;

        // 로그인 인증 처리
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
                        throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // REST API 이므로, 보안검사 끔
                                .csrf(csrf -> csrf.disable())
                                // 세션 사용 X, 로그인 상태의 인증 정보를 클라이언트(JWT)가 갖고 있음
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                // 권한 설정
                                .authorizeHttpRequests(auth -> auth
                                                // 누구나 접근 가능
                                                .requestMatchers("/", "/login", "/signup", "/find-id",
                                                                "/reset-password", "/new-password",
                                                                "/auth/**", "/oauth2/**", "/login/oauth2/**",
                                                                "/api/users/signup", "/api/users/login",
                                                                "/api/users/find-id",
                                                                "/api/users/reset-password",
                                                                "/api/users/reset-password-confirm",
                                                                "/api/users/check-username", "/api/users/check-email",
                                                                "/api/users/check-nickname",
                                                                "/api/emails/**",
                                                                "/ws/**", "/ws/chat/**",
                                                                "/error", "/css/**", "/js/**", "/images/**",
                                                                "/profiles/**", "/posts", "/posts/**",
                                                                "/static/**", "/*.html")
                                                .permitAll()
                                                // 조회만 허용
                                                .requestMatchers(HttpMethod.GET, "/api/posts/**", "/api/comments/**")
                                                .permitAll()
                                                // 관리자 페이지는 ADMIN 권한 필요
                                                .requestMatchers("/admin", "/admin/**")
                                                .hasRole("ADMIN")
                                                // 그 외 모든 요청은 인증 필요
                                                .anyRequest().authenticated())
                                // 인증 예외 처리 (API는 JSON 에러, 페이지는 리다이렉트)
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        String requestUri = request.getRequestURI();
                                                        // API 요청인 경우 JSON 에러 반환
                                                        if (requestUri.startsWith("/api/")) {
                                                                response.setStatus(401);
                                                                response.setContentType(
                                                                                "application/json;charset=UTF-8");
                                                                response.getWriter().write(
                                                                                "{\"error\":\"Unauthorized\",\"message\":\"인증이 필요합니다.\"}");
                                                        } else {
                                                                // 페이지 요청인 경우 로그인 페이지로 리다이렉트
                                                                response.sendRedirect("/login");
                                                        }
                                                }))
                                // 로그인 관련 설정
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .permitAll())
                                // OAuth2 로그인 설정
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login")
                                                .authorizationEndpoint(authorization -> authorization
                                                                .baseUri("/oauth2/authorization")
                                                                .authorizationRequestRepository(
                                                                                httpCookieOAuth2AuthorizationRequestRepository))
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .successHandler(oAuth2AuthenticationSuccessHandler)
                                                .failureHandler(oAuth2AuthenticationFailureHandler))
                                // 로그아웃 설정
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .addLogoutHandler(customLogoutHandler)
                                                .logoutSuccessUrl("/")
                                                .deleteCookies("access_token", "jwt_token", "refreshToken",
                                                                "JSESSIONID")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .permitAll())
                                // JWT 필터 등록
                                .addFilterBefore(
                                                new JwtAuthenticationFilter(jwtTokenProvider, userRepository,
                                                                jwtBlacklistService),
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        // 사용자 로그인 암호화
        @Bean
        public PasswordEncoder passwordEncoder() {
                // BCrypt Encoder 사용
                return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }
}
