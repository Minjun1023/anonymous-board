package com.example.anonymous_board.controllers.web;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatPageController {

    private final UserRepository userRepository;

    @GetMapping("/{receiverId}")
    public String chatPage(@AuthenticationPrincipal Member member, @PathVariable Long receiverId, Model model) {
        if (member == null) {
            return "redirect:/login";
        }
        Member receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        model.addAttribute("receiver", receiver);
        model.addAttribute("sender", member);
        return "chat";
    }
}
