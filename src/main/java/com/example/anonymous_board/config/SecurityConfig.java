package com.example.anonymous_board.config;

import com.example.anonymous_board.auth.oauth.CustomOAuth2UserService;
import com.example.anonymous_board.auth.oauth.OAuth2AuthenticationSuccessHandler;
import com.example.anonymous_board.config.jwt.JwtAuthenticationFilter;
import com.example.anonymous_board.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // http basic auth 기반 로그인 인증창 비활성화
                .httpBasic((httpBasic) -> httpBasic.disable())
                // csrf 비활성화
                .csrf((csrf) -> csrf.disable())
                // 세션 사용하지 않음
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((authorize) -> authorize
                        // 인증 없이 허용할 경로들
                        .requestMatchers(
                            "/users/signup", 
                            "/users/login", 
                            "/api/email/**", 
                            "/auth_success.html",
                            "/login",
                            "/oauth2/**",
                            "/login/oauth2/**"
                        ).permitAll()
                        // /users/test 경로는 USER 권한이 있어야만 허용
                        .requestMatchers("/users/test").hasRole("USER")
                        // 나머지 모든 경로는 인증 필요
                        .anyRequest().authenticated())
                // oauth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler))
                // JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 전에 추가
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt Encoder 사용
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
