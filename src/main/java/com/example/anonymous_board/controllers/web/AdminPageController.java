package com.example.anonymous_board.controllers.web;

import com.example.anonymous_board.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPageController {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String adminPage(Model model, @AuthenticationPrincipal Member member) {
        model.addAttribute("admin", member);
        return "admin";
    }
}
