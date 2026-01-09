package com.example.anonymous_board.service;

import com.example.anonymous_board.config.jwt.JwtTokenProvider;
import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Role;
import com.example.anonymous_board.dto.TokenInfo;
import com.example.anonymous_board.dto.UserSignupRequest;
import com.example.anonymous_board.repository.UserRepository;
import com.example.anonymous_board.repository.VoteRepository;
import com.example.anonymous_board.repository.PollVoteRepository;
import com.example.anonymous_board.repository.MessageRepository;
import com.example.anonymous_board.repository.UserConversationRepository;
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
    private final RedisEmailTokenService redisEmailTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final com.example.anonymous_board.repository.PostRepository postRepository;
    private final com.example.anonymous_board.repository.CommentRepository commentRepository;
    private final VoteRepository voteRepository;
    private final PollVoteRepository pollVoteRepository;
    private final MessageRepository messageRepository;
    private final UserConversationRepository userConversationRepository;

    // 생성자에서 @Lazy로 순환 참조 방지
    public UserService(
            UserRepository userRepository,
            RedisEmailTokenService redisEmailTokenService,
            PasswordEncoder passwordEncoder,
            @Lazy AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            com.example.anonymous_board.repository.PostRepository postRepository,
            com.example.anonymous_board.repository.CommentRepository commentRepository,
            VoteRepository voteRepository,
            PollVoteRepository pollVoteRepository,
            MessageRepository messageRepository,
            UserConversationRepository userConversationRepository) {
        this.userRepository = userRepository;
        this.redisEmailTokenService = redisEmailTokenService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.voteRepository = voteRepository;
        this.pollVoteRepository = pollVoteRepository;
        this.messageRepository = messageRepository;
        this.userConversationRepository = userConversationRepository;
    }

    /**
     * 회원가입
     */
    @Transactional
    public Long signup(UserSignupRequest request) {
        // Redis에서 이메일 인증 완료 상태 확인
        if (!redisEmailTokenService.isEmailVerified(request.getEmail())) {
            throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
        }

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

        return userRepository.save(user).getId();
    }

    /**
     * 아이디 중복 검사
     */
    public boolean isUsernameExist(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 이메일 중복 검사
     */
    public boolean isEmailExist(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 닉네임 중복 검사
     */
    public boolean isNicknameExist(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    /**
     * 로그인
     */
    @Transactional
    public TokenInfo login(String username, String password) {
        // 1. Login ID/PW 를 기반으로 Authentication 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
                password);

        // 2. 실제 검증 (사용자 비밀번호 체크)
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        return jwtTokenProvider.generateToken(authentication);
    }

    // 이메일로 사용자 이름 찾기
    public String findUsernameByEmail(String email) {
        Member member = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));
        if (!member.isEmailVerified()) {
            throw new IllegalArgumentException("이메일 인증이 완료되지 않은 계정입니다.");
        }
        return member.getUsername();
    }

    // 비밀번호 재설정
    @Transactional
    public void resetPassword(String token, String newPassword) {
        // Redis에서 토큰으로 이메일 조회
        String email = redisEmailTokenService.getEmailByPasswordResetToken(token);
        if (email == null) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 토큰입니다.");
        }

        Member member = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new IllegalArgumentException("이전에 사용한 비밀번호는 사용할 수 없습니다.");
        }

        member.updatePassword(passwordEncoder.encode(newPassword));

        // 사용된 토큰 삭제
        redisEmailTokenService.deletePasswordResetToken(token);
    }

    // 사용자 정보 로드
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
     * 프로필 이미지 수정 (userId로 조회)
     */
    @Transactional
    public void updateProfileImage(Long userId, String imageUrl) {
        Member user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.updateProfileImage(imageUrl);
    }

    /**
     * 닉네임 수정
     */
    @Transactional
    public void updateNickname(Member user, String newNickname) {
        // 현재 닉네임과 동일하면 변경할 필요 없음
        if (user.getNickname().equals(newNickname)) {
            return;
        }

        // 닉네임 중복 검사 (다른 사용자가 사용 중인지 확인)
        userRepository.findByNickname(newNickname).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(user.getId())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
        });

        // 사용자 닉네임 변경
        user.updateNickname(newNickname);

        // 작성한 게시글 및 댓글의 닉네임도 일괄 변경
        postRepository.updateNicknameByMember(user, newNickname);
        commentRepository.updateNicknameByMember(user, newNickname);
    }

    /**
     * 비밀번호 변경 (프로필 페이지에서)
     */
    @Transactional
    public void changePassword(Member user, String currentPassword, String newPassword) {
        // OAuth 사용자는 비밀번호가 없으므로 변경 불가
        if (user.getProvider() != null && !user.getProvider().isEmpty()) {
            throw new IllegalArgumentException("소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
        }

        // 비밀번호가 없는 경우 (OAuth 사용자)
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
        }

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호가 현재 비밀번호와 동일한지 확인
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        // 새 비밀번호 암호화 후 저장
        user.updatePassword(passwordEncoder.encode(newPassword));
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void deleteAccount(Member user, String password) {
        // OAuth 사용자가 아닌 경우에만 비밀번호 확인
        if (user.getProvider() == null || user.getProvider().isEmpty()) {
            // 일반 로그인 사용자: 비밀번호 확인 필수
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
        }
        // OAuth 사용자는 비밀번호 확인 없이 바로 탈퇴 진행

        // 외래키 제약조건을 일시적으로 비활성화하여 모든 관련 데이터 삭제
        commentRepository.disableForeignKeyChecks();

        try {
            // 관련 데이터 삭제
            // 1. 추천/비추천 투표 삭제 (본인이 한 투표)
            voteRepository.deleteByMember(user);

            // 2. 설문 투표 삭제 (본인이 한 투표)
            pollVoteRepository.deleteByMember(user);

            // 3. 메시지 삭제 (보낸 메시지와 받은 메시지 모두)
            messageRepository.deleteBySender(user);
            messageRepository.deleteByReceiver(user);

            // 4. 대화 정보 삭제
            userConversationRepository.deleteByUser(user);
            userConversationRepository.deleteByOtherUser(user);

            // 5. 본인 게시글에 달린 다른 사용자의 투표 삭제
            voteRepository.deleteByPostMember(user);

            // 6. 댓글 삭제 (본인이 작성한 댓글 + 본인 게시글에 달린 모든 댓글)
            commentRepository.deleteAllCommentsByUser(user.getId());

            // 7. 게시글 삭제 (PostImage, Poll, PollOption 등 포함)
            postRepository.deleteByMember(user);

            // 8. 사용자 삭제
            userRepository.delete(user);
        } finally {
            // 외래키 제약조건 다시 활성화 (에러 발생 여부와 관계없이 실행)
            commentRepository.enableForeignKeyChecks();
        }
    }

    private UserDetails createUserDetails(Member user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
