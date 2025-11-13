package com.example.anonymous_board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageCreateRequest {
    private Long receiverId;
    private String content;
}
