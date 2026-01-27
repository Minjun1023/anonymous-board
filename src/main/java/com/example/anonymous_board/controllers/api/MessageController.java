package com.example.anonymous_board.controllers.api;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.dto.ConversationDto;
import com.example.anonymous_board.dto.MessageCreateRequest;
import com.example.anonymous_board.dto.MessageDto;
import com.example.anonymous_board.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // 대화 조회
    @GetMapping("/{receiverId}")
    public ResponseEntity<ConversationDto> getConversation(@AuthenticationPrincipal Member member,
            @PathVariable Long receiverId) {
        if (member == null) {
            return ResponseEntity.status(401).build();
        }
        ConversationDto conversation = messageService.getConversation(member.getId(), receiverId);
        return ResponseEntity.ok(conversation);
    }

    // 메시지 전송
    @PostMapping
    public ResponseEntity<MessageDto> sendMessage(@AuthenticationPrincipal Member member,
            @RequestBody MessageCreateRequest request) {
        if (member == null) {
            return ResponseEntity.status(401).build();
        }
        MessageDto messageDto = messageService.sendMessage(member.getId(), request);
        return ResponseEntity.ok(messageDto);
    }

    // 대화 나가기
    @PostMapping("/leave/{otherUserId}")
    public ResponseEntity<Void> leaveChat(@AuthenticationPrincipal Member member, @PathVariable Long otherUserId) {
        if (member == null) {
            return ResponseEntity.status(401).build();
        }
        messageService.leaveChat(member.getId(), otherUserId);
        return ResponseEntity.ok().build();
    }

    // 메시지 읽음 처리
    @PostMapping("/messages/{messageId}/read")
    public ResponseEntity<Void> markMessageAsRead(@AuthenticationPrincipal Member member,
            @PathVariable Long messageId) {
        if (member == null) {
            return ResponseEntity.status(401).build();
        }
        messageService.markMessageAsRead(messageId, member.getId());
        return ResponseEntity.ok().build();
    }
}
