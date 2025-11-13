package com.example.anonymous_board.controllers.web;

import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.domain.Post;
import com.example.anonymous_board.repository.UserRepository;
import com.example.anonymous_board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfilePageController {

    private final UserRepository userRepository;
    private final PostService postService;

    // 프로필 페이지
    @GetMapping
    public String profilePage(Model model, @AuthenticationPrincipal Member member) {
        model.addAttribute("user", member);
        return "profile";
    }

    @GetMapping("/posts")
    public String userPostsPage(@RequestParam Long userId, Model model) {
        Member user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        List<Post> posts = postService.getPostsByMember(user);
        model.addAttribute("user", user);
        model.addAttribute("posts", posts);
        return "user-posts";
    }
}
