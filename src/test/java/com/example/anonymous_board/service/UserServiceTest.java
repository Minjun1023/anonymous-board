package com.example.anonymous_board.service;

import com.example.anonymous_board.config.jwt.JwtTokenProvider;
import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Role;
import com.example.anonymous_board.repository.*;
import com.example.anonymous_board.service.RedisEmailTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisEmailTokenService redisEmailTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private PollVoteRepository pollVoteRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserConversationRepository userConversationRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                redisEmailTokenService,
                passwordEncoder,
                authenticationManager,
                jwtTokenProvider,
                postRepository,
                commentRepository,
                voteRepository,
                pollVoteRepository,
                messageRepository,
                userConversationRepository);
    }

    @Test
    @DisplayName("아이디 존재 확인 - 존재하는 경우 true 반환")
    void isUsernameExist_ReturnsTrue_WhenExists() {
        // given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // when
        boolean result = userService.isUsernameExist("testuser");

        // then
        assertThat(result).isTrue();
        verify(userRepository, times(1)).existsByUsername("testuser");
    }

    @Test
    @DisplayName("아이디 존재 확인 - 존재하지 않는 경우 false 반환")
    void isUsernameExist_ReturnsFalse_WhenNotExists() {
        // given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        // when
        boolean result = userService.isUsernameExist("newuser");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("이메일 존재 확인 - 존재하는 경우 true 반환")
    void isEmailExist_ReturnsTrue_WhenExists() {
        // given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // when
        boolean result = userService.isEmailExist("test@example.com");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("닉네임 존재 확인 - 존재하는 경우 true 반환")
    void isNicknameExist_ReturnsTrue_WhenExists() {
        // given
        when(userRepository.existsByNickname("테스트")).thenReturn(true);

        // when
        boolean result = userService.isNicknameExist("테스트");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("사용자 조회 - 존재하는 사용자")
    void loadUserByUsername_Success() {
        // given
        Member member = Member.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("테스트유저")
                .role(Role.USER)
                .provider("local")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(member));

        // when
        UserDetails userDetails = userService.loadUserByUsername("testuser");

        // then
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("사용자 조회 - 존재하지 않는 사용자 예외 발생")
    void loadUserByUsername_ThrowsException_WhenNotFound() {
        // given
        when(userRepository.findByUsername("notexist")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.loadUserByUsername("notexist"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("해당하는 유저를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("사용자 조회 by Username - 존재하는 사용자")
    void findByUsername_Success() {
        // given
        Member member = Member.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("테스트유저")
                .role(Role.USER)
                .provider("local")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(member));

        // when
        Member result = userService.findByUsername("testuser");

        // then
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("사용자 조회 by Username - 존재하지 않는 사용자 예외 발생")
    void findByUsername_ThrowsException_WhenNotFound() {
        // given
        when(userRepository.findByUsername("notexist")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findByUsername("notexist"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }
}
