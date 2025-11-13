package com.example.anonymous_board.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ConversationDto {
    private List<MessageDto> messages;
    private boolean otherUserHasLeft;
}
