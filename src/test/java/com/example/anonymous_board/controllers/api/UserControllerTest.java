package com.example.anonymous_board.controllers.api;

import com.example.anonymous_board.dto.UserSignupRequest;
import com.example.anonymous_board.service.EmailService;
import com.example.anonymous_board.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // Spring Security 필터 비활성화
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc; // MockMvc를 사용하여 HTTP 요청 시뮬레이션

    @MockBean
    private UserService userService; // 사용자 서비스 Mock

    @MockBean
    private EmailService emailService; // 이메일 서비스 Mock

    @Autowired
    private ObjectMapper objectMapper; // JSON 변환을 위한 ObjectMapper

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signup_Success() throws Exception {
        // given
        UserSignupRequest request = new UserSignupRequest();
        request.setUsername("testuser123");
        request.setEmail("test@example.com");
        request.setPassword("StrongPass1!"); // 대문자, 소문자, 숫자, 특수문자 포함
        request.setNickname("테스트닉네임");

        // service가 호출되면 성공 가정 (Long 반환)
        Mockito.when(userService.signup(any(UserSignupRequest.class)))
                .thenReturn(1L);

        // when & then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("회원가입이 성공적으로 완료되었습니다."));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 공백 (필수값 누락)")
    void signup_Fail_BlankEmail() throws Exception {
        // given
        UserSignupRequest request = new UserSignupRequest();
        request.setUsername("testuser123");
        request.setEmail(""); // 빈 문자열 (@NotBlank 위반)
        request.setPassword("StrongPass1!");
        request.setNickname("테스트닉네임");

        // when & then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // 400 Bad Request
    }

    @Test
    @DisplayName("회원가입 실패 - 유효하지 않은 이메일 형식")
    void signup_Fail_InvalidEmail() throws Exception {
        // given
        UserSignupRequest request = new UserSignupRequest();
        request.setUsername("testuser123");
        request.setEmail("invalid-email"); // 잘못된 이메일 (without @)
        request.setPassword("StrongPass1!");
        request.setNickname("테스트닉네임");

        // when & then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // 400 Bad Request
    }

    @Test
    @DisplayName("회원가입 실패 - 아이디 규칙 위반 (특수문자 포함)")
    void signup_Fail_InvalidUsername() throws Exception {
        // given
        UserSignupRequest request = new UserSignupRequest();
        request.setUsername("user!@#"); // 특수문자 불가 (영문 소문자, 숫자만 가능)
        request.setEmail("test@example.com");
        request.setPassword("StrongPass1!");
        request.setNickname("테스트닉네임");

        // when & then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // 400 Bad Request
    }
}
