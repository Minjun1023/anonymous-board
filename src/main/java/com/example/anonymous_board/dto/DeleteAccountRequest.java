package com.example.anonymous_board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteAccountRequest {
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password; // 비밀번호
}
