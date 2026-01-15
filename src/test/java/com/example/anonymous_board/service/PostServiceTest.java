package com.example.anonymous_board.service;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Post;
import com.example.anonymous_board.domain.Role;
import com.example.anonymous_board.domain.Vote;
import com.example.anonymous_board.domain.VoteType;
import com.example.anonymous_board.repository.PollOptionRepository;
import com.example.anonymous_board.repository.PollVoteRepository;
import com.example.anonymous_board.repository.PostRepository;
import com.example.anonymous_board.repository.UserRepository;
import com.example.anonymous_board.repository.VoteRepository;
import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private PollOptionRepository pollOptionRepository;

    @Mock
    private PollVoteRepository pollVoteRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private HotPostsCacheService hotPostsCacheService;

    @Mock
    private UserRepository userRepository;

    private PostService postService;

    @BeforeEach
    void setUp() {
        postService = new PostService(
                postRepository,
                voteRepository,
                pollOptionRepository,
                pollVoteRepository,
                fileStorageService,
                hotPostsCacheService,
                userRepository);
    }

    // 테스트용 Member 생성 헬퍼
    private Member createTestMember(Long id, String username) {
        Member member = Member.builder()
                .username(username)
                .email(username + "@example.com")
                .nickname("테스트" + id)
                .role(Role.USER)
                .provider("local")
                .build();

        // Reflection을 사용하여 ID 설정 (builder에 id가 없으므로)
        try {
            Field idField = Member.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(member, id);
        } catch (Exception e) {
            throw new RuntimeException("멤버 ID 설정 실패", e);
        }

        return member;
    }

    // 테스트용 Post 생성 헬퍼
    private Post createTestPost(Long id, Member author) {
        Post post = new Post();
        post.setId(id);
        post.setTitle("테스트 게시글");
        post.setContent("테스트 내용");
        post.setMember(author);
        post.setNickname(author.getNickname());
        return post;
    }

    @Test
    @DisplayName("추천 투표 - 첫 투표 성공")
    void vote_Like_FirstVote_Success() {
        // given
        Member author = createTestMember(1L, "author");
        Member voter = createTestMember(2L, "voter");
        Post post = createTestPost(1L, author);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(voteRepository.findByMemberAndPost(voter, post)).thenReturn(Optional.empty());

        // when
        postService.vote(1L, voter, VoteType.LIKE);

        // then
        assertThat(post.getLikes()).isEqualTo(1);
        assertThat(post.getDislikes()).isEqualTo(0);
        verify(voteRepository).save(any(Vote.class));
        verify(hotPostsCacheService).invalidateCache(); // 캐시 무효화 확인
    }

    @Test
    @DisplayName("비추천 투표 - 첫 투표 성공")
    void vote_Dislike_FirstVote_Success() {
        // given
        Member author = createTestMember(1L, "author");
        Member voter = createTestMember(2L, "voter");
        Post post = createTestPost(1L, author);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(voteRepository.findByMemberAndPost(voter, post)).thenReturn(Optional.empty());

        // when
        postService.vote(1L, voter, VoteType.DISLIKE);

        // then
        assertThat(post.getLikes()).isEqualTo(0);
        assertThat(post.getDislikes()).isEqualTo(1);
        verify(voteRepository).save(any(Vote.class));
        verify(hotPostsCacheService).invalidateCache();
    }

    @Test
    @DisplayName("자신의 게시글에 투표 시도 - 예외 발생")
    void vote_OwnPost_ThrowsException() {
        // given
        Member author = createTestMember(1L, "author");
        Post post = createTestPost(1L, author);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.vote(1L, author, VoteType.LIKE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("자신의 글에는 추천/비추천할 수 없습니다");

        verify(voteRepository, never()).save(any());
        verify(hotPostsCacheService, never()).invalidateCache();
    }

    @Test
    @DisplayName("중복 추천 투표 - 투표 취소")
    void vote_Like_Duplicate_CancelVote() {
        // given
        Member author = createTestMember(1L, "author");
        Member voter = createTestMember(2L, "voter");
        Post post = createTestPost(1L, author);
        post.setLikes(1); // 이미 추천 1개

        Vote existingVote = new Vote(voter, post, VoteType.LIKE);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(voteRepository.findByMemberAndPost(voter, post)).thenReturn(Optional.of(existingVote));

        // when
        postService.vote(1L, voter, VoteType.LIKE);

        // then
        assertThat(post.getLikes()).isEqualTo(0); // 추천 취소됨
        verify(voteRepository).delete(existingVote);
        verify(hotPostsCacheService).invalidateCache();
    }

    @Test
    @DisplayName("투표 변경 - 비추천에서 추천으로")
    void vote_ChangeFromDislikeToLike() {
        // given
        Member author = createTestMember(1L, "author");
        Member voter = createTestMember(2L, "voter");
        Post post = createTestPost(1L, author);
        post.setDislikes(1); // 이미 비추천 1개

        Vote existingVote = new Vote(voter, post, VoteType.DISLIKE);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(voteRepository.findByMemberAndPost(voter, post)).thenReturn(Optional.of(existingVote));

        // when
        postService.vote(1L, voter, VoteType.LIKE);

        // then
        assertThat(post.getLikes()).isEqualTo(1); // 추천 증가
        assertThat(post.getDislikes()).isEqualTo(0); // 비추천 감소
        assertThat(existingVote.getVoteType()).isEqualTo(VoteType.LIKE); // 투표 타입 변경
        verify(voteRepository).save(existingVote);
        verify(hotPostsCacheService).invalidateCache();
    }

    @Test
    @DisplayName("투표 변경 - 추천에서 비추천으로")
    void vote_ChangeFromLikeToDislike() {
        // given
        Member author = createTestMember(1L, "author");
        Member voter = createTestMember(2L, "voter");
        Post post = createTestPost(1L, author);
        post.setLikes(1); // 이미 추천 1개

        Vote existingVote = new Vote(voter, post, VoteType.LIKE);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(voteRepository.findByMemberAndPost(voter, post)).thenReturn(Optional.of(existingVote));

        // when
        postService.vote(1L, voter, VoteType.DISLIKE);

        // then
        assertThat(post.getLikes()).isEqualTo(0); // 추천 감소
        assertThat(post.getDislikes()).isEqualTo(1); // 비추천 증가
        assertThat(existingVote.getVoteType()).isEqualTo(VoteType.DISLIKE);
        verify(voteRepository).save(existingVote);
        verify(hotPostsCacheService).invalidateCache();
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 투표 - 예외 발생")
    void vote_PostNotFound_ThrowsException() {
        // given
        Member voter = createTestMember(2L, "voter");
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.vote(999L, voter, VoteType.LIKE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("게시글 조회 성공")
    void getPostById_Success() {
        // given
        Member author = createTestMember(1L, "author");
        Post post = createTestPost(1L, author);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // when
        Post result = postService.getPostById(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("테스트 게시글");
    }

    @Test
    @DisplayName("핫 게시글 조회 성공")
    void getHotPosts_Success() {
        // given
        Member author = createTestMember(1L, "author");
        Post hotPost = createTestPost(1L, author);
        hotPost.setLikes(15);
        hotPost.setDislikes(3); // 네트 스코어 12점

        PageImpl<Post> page = new PageImpl<>(
                Collections.singletonList(hotPost),
                PageRequest.of(0, 10),
                1);

        when(postRepository.findHotPosts(eq(10), any())).thenReturn(page);

        // when
        Page<Post> result = postService.getHotPosts(0, 10);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getLikes()).isEqualTo(15);
        assertThat(result.getContent().get(0).getDislikes()).isEqualTo(3);
    }

    @Test
    @DisplayName("조회수 증가 성공")
    void incrementViewCount_Success() {
        // given
        Member author = createTestMember(1L, "author");
        Post post = createTestPost(1L, author);
        post.setViewCount(5);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // when
        postService.incrementViewCount(1L);

        // then
        assertThat(post.getViewCount()).isEqualTo(6);
    }

    @Test
    @DisplayName("게시글 삭제 - 본인 확인 실패 시 예외 발생")
    void deletePost_NotOwner_ThrowsException() {
        // given
        Member author = createTestMember(1L, "author");
        Member otherUser = createTestMember(2L, "other");
        Post post = createTestPost(1L, author);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.deletePost(1L, otherUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인이 작성한 게시글만 삭제할 수 있습니다");

        verify(postRepository, never()).delete(any());
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost_Success() {
        // given
        Member author = createTestMember(1L, "author");
        Post post = createTestPost(1L, author);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // when
        postService.deletePost(1L, author);

        // then
        verify(postRepository).delete(post);
    }
}
