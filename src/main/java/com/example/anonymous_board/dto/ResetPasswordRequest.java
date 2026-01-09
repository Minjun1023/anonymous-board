package com.example.anonymous_board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String username; // 사용자 ID

}
