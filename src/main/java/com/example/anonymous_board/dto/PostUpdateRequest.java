package com.example.anonymous_board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostUpdateRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    private String title; // 게시글 제목

    @NotBlank(message = "내용을 입력해주세요.")
    private String content; // 게시글 내용

    private List<Long> deleteImageIds; // 삭제할 이미지 ID 리스트
}
