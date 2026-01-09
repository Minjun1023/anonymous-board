package com.example.anonymous_board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "현재 비밀번호는 필수 입력 값입니다.")
    private String currentPassword; // 현재 비밀번호

    @NotBlank(message = "새 비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,16}$", message = "비밀번호는 8~16자의 영문 대소문자, 숫자, 특수문자를 포함해야 합니다.")
    private String newPassword; // 새 비밀번호

    @NotBlank(message = "새 비밀번호 확인은 필수 입력 값입니다.")
    private String confirmPassword; // 새 비밀번호 확인
}
