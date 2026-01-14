package com.example.anonymous_board.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SuspendUserRequest {

    private Integer suspendDays; // 정지 일수 (null이면 영구 정지)
    private String reason; // 정지 사유
}
