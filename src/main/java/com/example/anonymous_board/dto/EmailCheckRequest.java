package com.example.anonymous_board.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmailCheckRequest {

    @Email
    @NotBlank
    private String email; // 이메일

    @NotBlank
    private String token; // 토큰
}
