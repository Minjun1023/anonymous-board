package com.example.anonymous_board.service;

import com.example.anonymous_board.config.jwt.JwtTokenProvider;
import com.example.anonymous_board.domain.EmailVerificationToken;
import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Role;
import com.example.anonymous_board.domain.TokenType;
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

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    // 생성자에서 @Lazy로 순환 참조 방지
    public UserService(
            UserRepository userRepository,
            EmailVerificationTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            @Lazy AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider) {
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
        EmailVerificationToken token = tokenRepository.findByEmailAndTokenType(request.getEmail(), TokenType.EMAIL_VERIFICATION)
                .filter(t -> t.isVerified() && !t.isExpired())
                .orElseThrow(() -> new IllegalArgumentException("이메일 인증이 완료되지 않았습니다."));

        // 아이디 중복 검사
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
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
        Member user = Member.builder()
                .username(request.getUsername())
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

    public boolean isUsernameExist(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean isEmailExist(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isNicknameExist(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    /**
     * 로그인
     */
    @Transactional
    public TokenInfo login(String username, String password) {
        // 1. Login ID/PW 를 기반으로 Authentication 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        // 2. 실제 검증 (사용자 비밀번호 체크)
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        return jwtTokenProvider.generateToken(authentication);
    }

    public String findUsernameByEmail(String email) {
        Member member = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));
        if (!member.isEmailVerified()) {
            throw new IllegalArgumentException("이메일 인증이 완료되지 않은 계정입니다.");
        }
        return member.getUsername();
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        EmailVerificationToken passwordResetToken = tokenRepository.findByToken(token)
                .filter(t -> t.getTokenType() == TokenType.PASSWORD_RESET && !t.isExpired())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));

        if (passwordResetToken.getExpirationTime().isBefore(LocalDateTime.now())) {
            passwordResetToken.setExpired();
            throw new IllegalArgumentException("토큰이 만료되었습니다.");
        }

        Member member = userRepository.findByEmail(passwordResetToken.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new IllegalArgumentException("이전에 사용한 비밀번호는 사용할 수 없습니다.");
        }

        member.updatePassword(passwordEncoder.encode(newPassword));
        passwordResetToken.setExpired();
        tokenRepository.delete(passwordResetToken);
        tokenRepository.flush();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(this::createUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("해당하는 유저를 찾을 수 없습니다."));
    }

    // 아이디로 사용자 조회
    public Member findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    // 이메일로 사용자 조회
    public Member findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 프로필 이미지 수정
     */
    @Transactional
    public void updateProfileImage(String email, String imageUrl) {
        Member user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.updateProfileImage(imageUrl);
    }

    private UserDetails createUserDetails(Member user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
