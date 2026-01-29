package com.example.anonymous_board.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.anonymous_board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import com.example.anonymous_board.domain.Post;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final PostService postService;

    // 메인 페이지
    @GetMapping("/")
    public String home(Model model) {
        // 핫 게시글 상위 5개 가져오기
        Page<Post> hotPosts = postService.getHotPosts(0, 5);
        model.addAttribute("hotPosts", hotPosts.getContent());

        // 최신 게시글 상위 5개 가져오기
        Page<Post> recentPosts = postService.getAllPosts(0, 5, "latest");
        model.addAttribute("recentPosts", recentPosts.getContent());

        return "index";
    }

    // 게시글 목록
    @GetMapping("/posts")
    public String showPostList(@RequestParam(required = false) String boardType, Model model) {
        // boardType 파라미터를 뷰로 전달 (탭 활성화 및 API 호출에 사용)
        model.addAttribute("currentBoardType", boardType != null ? boardType : "free");
        return "posts/list";
    }

    // 핫 게시판
    @GetMapping("/posts/hot")
    public String showHotPostList() {
        return "posts/hot-posts";
    }

    // 게시글 작성 페이지
    @GetMapping("/posts/write")
    public String showWriteForm() {
        return "posts/write";
    }

    // 게시글 상세 페이지
    @GetMapping("/posts/{id}")
    public String showPostDetail() {
        return "posts/detail";
    }

    // 게시글 수정 페이지
    @GetMapping("/posts/{id}/edit")
    public String showEditForm() {
        return "posts/edit";
    }

    // 로그인 페이지
    @GetMapping({ "/login", "/auth/login" })
    public String loginPage() {
        return "login";
    }

    // 회원가입 페이지
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    // 아이디 찾기
    @GetMapping("/find-id")
    public String findId() {
        return "find-id";
    }

    // 비밀번호 재설정
    @GetMapping("/reset-password")
    public String resetPassword() {
        return "reset-password";
    }

    // 새 비밀번호 설정
    @GetMapping("/new-password")
    public String showNewPasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "new-password";
    }
}