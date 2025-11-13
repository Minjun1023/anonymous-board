package com.example.anonymous_board.controllers.web;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.dto.ConversationSummaryDto;
import com.example.anonymous_board.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/my-chats")
@RequiredArgsConstructor
public class MyChatsPageController {

    private final MessageService messageService;

    @GetMapping
    public String myChatsPage(@AuthenticationPrincipal Member member, Model model) {
        if (member == null) {
            return "redirect:/login";
        }
        List<ConversationSummaryDto> conversations = messageService.getConversationSummaries(member.getId());
        model.addAttribute("conversations", conversations);
        return "my-chats";
    }
}
