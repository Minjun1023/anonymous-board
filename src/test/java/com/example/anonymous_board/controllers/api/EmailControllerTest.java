package com.example.anonymous_board.controllers.api;

import com.example.anonymous_board.dto.EmailAuthRequest;
import com.example.anonymous_board.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailController.class)
@AutoConfigureMockMvc(addFilters = false) // Spring Security 필터 비활성화 (순수 컨트롤러 로직만 테스트)
class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("인증 이메일 발송 성공 테스트")
    void sendVerificationEmail_Success() throws Exception {
        // given
        String email = "test@example.com";
        EmailAuthRequest request = new EmailAuthRequest();
        request.setEmail(email);

        // service가 호출되면 아무 일도 안 함 (성공 가정)
        doNothing().when(emailService).sendVerificationEmail(anyString());

        // when & then
        mockMvc.perform(post("/api/emails/send-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("인증 이메일이 성공적으로 발송되었습니다. 이메일을 확인해주세요."));
    }

    @Test
    @DisplayName("잘못된 이메일 형식으로 발송 요청 시 400 에러")
    void sendVerificationEmail_Invalid() throws Exception {
        // given
        String invalidEmail = "invalid-email"; // @ 없음
        EmailAuthRequest request = new EmailAuthRequest();
        request.setEmail(invalidEmail);

        // when & then
        mockMvc.perform(post("/api/emails/send-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
