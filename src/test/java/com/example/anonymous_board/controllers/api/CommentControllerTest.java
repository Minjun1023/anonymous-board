package com.example.anonymous_board.controllers.api;

import com.example.anonymous_board.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false) // Spring Security 필터 비활성화
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @Test
    @DisplayName("댓글 목록 조회 성공")
    void getComments_Success() throws Exception {
        // given - 빈 리스트 반환 (CommentResponse 생성 시 NPE 방지)
        Mockito.when(commentService.getCommentsByPostId(anyLong(), any()))
                .thenReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("댓글 작성 - 비로그인 시 401 에러")
    void createComment_Unauthorized() throws Exception {
        // when & then (AuthenticationPrincipal이 null인 경우)
        mockMvc.perform(post("/api/posts/1/comments")
                .contentType("application/json")
                .content("{\"content\": \"테스트 댓글\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("댓글 삭제 - 비로그인 시 401 에러")
    void deleteComment_Unauthorized() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/comments/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("댓글 수정 - 비로그인 시 401 에러")
    void updateComment_Unauthorized() throws Exception {
        // when & then
        mockMvc.perform(put("/api/comments/1")
                .contentType("application/json")
                .content("{\"content\": \"수정된 댓글\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인이 필요합니다."));
    }
}
