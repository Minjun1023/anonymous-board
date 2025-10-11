package com.example.anonymous_board.service;

import com.example.anonymous_board.config.jwt.JwtTokenProvider;
import com.example.anonymous_board.domain.EmailVerificationToken;
import com.example.anonymous_board.domain.Role;
import com.example.anonymous_board.domain.User;
import com.example.anonymous_board.dto.TokenInfo;
import com.example.anonymous_board.dto.UserSignupRequest;
import com.example.anonymous_board.repository.EmailVerificationTokenRepository;
import com.example.anonymous_board.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository, EmailVerificationTokenRepository tokenRepository, PasswordEncoder passwordEncoder, @Lazy AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 회원가입
     */
    @Transactional
    public Long signup(UserSignupRequest request) {
        // 이메일 인증을 통과했는지 확인
        EmailVerificationToken token = tokenRepository.findByEmail(request.getEmail())
                .filter(t -> t.isVerified() && !t.isExpired())
                .orElseThrow(() -> new IllegalArgumentException("이메일 인증이 완료되지 않았습니다."));

        // 이메일 중복 검사
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        // 닉네임 중복 검사
        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .role(Role.USER)
                .emailVerified(true) // 이메일 인증 완료
                .provider("local") // 로컬 가입
                .build();
        
        // 사용 완료된 토큰은 만료시킴
        token.setExpired();

        return userRepository.save(user).getId();
    }

    /**
     * 로그인
     */
    @Transactional
    public TokenInfo login(String email, String password) {
        // 1. Login ID/PW 를 기반으로 Authentication 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);

        // 2. 실제 검증 (사용자 비밀번호 체크)
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        return jwtTokenProvider.generateToken(authentication);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(this::createUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("해당하는 유저를 찾을 수 없습니다."));
    }

    private UserDetails createUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().getKey()) // "ROLE_USER"
                .build();
    }
}
