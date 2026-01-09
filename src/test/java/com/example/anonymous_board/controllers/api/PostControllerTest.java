package com.example.anonymous_board.controllers.api;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Post;
import com.example.anonymous_board.domain.Role;
import com.example.anonymous_board.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false) // Spring Security 필터 비활성화
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    // 테스트용 Member 생성 헬퍼
    private Member createTestMember() {
        return Member.builder()
                .username("testuser")
                .email("test@example.com")
                .nickname("테스트유저")
                .role(Role.USER)
                .provider("local")
                .build();
    }

    // 테스트용 Post 생성 헬퍼 (Post는 @Setter가 있어서 setter 사용)
    private Post createTestPost(Member member) {
        Post post = new Post();
        post.setId(1L);
        post.setTitle("테스트 게시글");
        post.setContent("테스트 내용입니다.");
        post.setMember(member);
        post.setNickname(member.getNickname());
        post.setViewCount(0);
        post.setLikes(0);
        post.setDislikes(0);
        return post;
    }

    @Test
    @DisplayName("게시글 전체 조회 성공")
    void getAllPosts_Success() throws Exception {
        // given
        Member member = createTestMember();
        Post post = createTestPost(member);
        PageImpl<Post> postPage = new PageImpl<>(
                Collections.singletonList(post),
                PageRequest.of(0, 10),
                1);

        Mockito.when(postService.getAllPosts(anyInt(), anyInt(), anyString()))
                .thenReturn(postPage);

        // when & then
        mockMvc.perform(get("/api/posts")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("게시글 단건 조회 성공")
    void getPostById_Success() throws Exception {
        // given
        Member member = createTestMember();
        Post post = createTestPost(member);

        Mockito.doNothing().when(postService).incrementViewCount(anyLong());
        Mockito.when(postService.getPostById(1L)).thenReturn(post);

        // when & then
        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("테스트 게시글"));
    }

    @Test
    @DisplayName("게시글 삭제 - 비로그인 시 401 에러")
    void deletePost_Unauthorized() throws Exception {
        // when & then (AuthenticationPrincipal이 null인 경우)
        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("게시글 검색 성공")
    void searchPosts_Success() throws Exception {
        // given
        Member member = createTestMember();
        Post post = createTestPost(member);
        PageImpl<Post> postPage = new PageImpl<>(
                Collections.singletonList(post),
                PageRequest.of(0, 10),
                1);

        Mockito.when(postService.searchPosts(anyString(), anyInt(), anyInt()))
                .thenReturn(postPage);

        // when & then
        mockMvc.perform(get("/api/posts/search")
                .param("keyword", "테스트")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("핫 게시글 조회 성공")
    void getHotPosts_Success() throws Exception {
        // given
        Member member = createTestMember();
        Post post = createTestPost(member);
        PageImpl<Post> postPage = new PageImpl<>(
                Collections.singletonList(post),
                PageRequest.of(0, 10),
                1);

        Mockito.when(postService.getHotPosts(anyInt(), anyInt()))
                .thenReturn(postPage);

        // when & then
        mockMvc.perform(get("/api/posts/hot")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
