
package com.example.anonymous_board.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSignupRequest {

    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    @Pattern(regexp = "^[a-z0-9]{4,12}$", message = "아이디는 4~12자의 영문 소문자, 숫자로만 입력해주세요.")
    private String username; // 사용자 ID

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "유효하지 않은 이메일 형식입니다.")
    private String email; // 사용자 이메일

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,16}$", message = "비밀번호는 8~16자의 영문 대소문자, 숫자, 특수문자를 포함해야 합니다.")
    private String password; // 사용자 비밀번호

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9]{2,10}$", message = "닉네임은 2~10자의 한글, 영문, 숫자로만 입력해주세요.")
    private String nickname; // 사용자 닉네임
}
