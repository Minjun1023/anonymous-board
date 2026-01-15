package com.example.anonymous_board.service;

import com.example.anonymous_board.domain.Comment;
import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Post;
import com.example.anonymous_board.domain.Role;
import com.example.anonymous_board.dto.CommentCreateRequest;
import com.example.anonymous_board.dto.CommentUpdateRequest;
import com.example.anonymous_board.repository.CommentRepository;
import com.example.anonymous_board.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentRepository, postRepository);
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
        post.setCommentCount(0);
        return post;
    }

    // 테스트용 Comment 생성 헬퍼
    private Comment createTestComment(Long id, Post post, Member author, String content) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setContent(content);
        comment.setNickname(author.getNickname());
        comment.setMember(author);
        comment.setPost(post);
        return comment;
    }

    @Test
    @DisplayName("댓글 작성 - 성공")
    void createComment_Success() {
        // given
        Member author = createTestMember(1L, "author");
        Post post = createTestPost(1L, author);
        Member commenter = createTestMember(2L, "commenter");

        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("테스트 댓글입니다");
        request.setSecret(false);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // when
        commentService.createComment(1L, request, commenter);

        // then
        verify(commentRepository).save(any(Comment.class));
        verify(postRepository).save(post);
        assertThat(post.getCommentCount()).isEqualTo(1); // 댓글 수 증가 확인
    }

    @Test
    @DisplayName("댓글 작성 - 존재하지 않는 게시글에 댓글 작성 시 예외")
    void createComment_PostNotFound_ThrowsException() {
        // given
        Member commenter = createTestMember(2L, "commenter");
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("댓글 내용");

        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.createComment(999L, request, commenter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("대댓글 작성 - 부모 댓글이 다른 게시글에 있을 때 예외")
    void createComment_ParentInDifferentPost_ThrowsException() {
        // given
        Member author = createTestMember(1L, "author");
        Post post1 = createTestPost(1L, author);
        Post post2 = createTestPost(2L, author);
        Comment parentComment = createTestComment(1L, post2, author, "부모 댓글");

        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("대댓글 내용");
        request.setParentId(1L); // post2에 있는 댓글

        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(parentComment));

        // when & then
        assertThatThrownBy(() -> commentService.createComment(1L, request, author))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("부모 댓글이 해당 게시글에 존재하지 않습니다");
    }

    @Test
    @DisplayName("댓글 삭제 - 본인 댓글 삭제 성공")
    void deleteComment_OwnComment_Success() {
        // given
        Member author = createTestMember(1L, "author");
        Post post = createTestPost(1L, author);
        post.setCommentCount(1); // 기존 댓글 1개

        Comment comment = createTestComment(1L, post, author, "내 댓글");

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        // when
        commentService.deleteComment(1L, author);

        // then
        verify(commentRepository).delete(comment);
        verify(postRepository).save(post);
        assertThat(post.getCommentCount()).isEqualTo(0); // 댓글 수 감소 확인
    }

    @Test
    @DisplayName("댓글 삭제 - 타인 댓글 삭제 시도 시 예외 (보안 필수)")
    void deleteComment_OthersComment_ThrowsException() {
        // given
        Member author = createTestMember(1L, "author");
        Member otherUser = createTestMember(2L, "other");
        Post post = createTestPost(1L, author);
        Comment comment = createTestComment(1L, post, author, "작성자의 댓글");

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(1L, otherUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인이 작성한 댓글만 삭제할 수 있습니다");

        // 댓글이 삭제되지 않았는지 확인
        verify(commentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("댓글 삭제 - 존재하지 않는 댓글 삭제 시 예외")
    void deleteComment_NotFound_ThrowsException() {
        // given
        Member user = createTestMember(1L, "user");
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(999L, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("댓글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("댓글 수정 - 본인 댓글 수정 성공")
    void updateComment_OwnComment_Success() {
        // given
        Member author = createTestMember(1L, "author");
        Post post = createTestPost(1L, author);
        Comment comment = createTestComment(1L, post, author, "원래 내용");

        CommentUpdateRequest request = new CommentUpdateRequest();
        request.setContent("수정된 내용");

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        // when
        commentService.updateComment(1L, request, author);

        // then
        assertThat(comment.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("댓글 수정 - 타인 댓글 수정 시도 시 예외 (보안 필수)")
    void updateComment_OthersComment_ThrowsException() {
        // given
        Member author = createTestMember(1L, "author");
        Member otherUser = createTestMember(2L, "other");
        Post post = createTestPost(1L, author);
        Comment comment = createTestComment(1L, post, author, "작성자의 댓글");

        CommentUpdateRequest request = new CommentUpdateRequest();
        request.setContent("수정 시도");

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(1L, request, otherUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인이 작성한 댓글만 수정할 수 있습니다");

        // 댓글 내용이 변경되지 않았는지 확인
        assertThat(comment.getContent()).isEqualTo("작성자의 댓글");
    }
}
