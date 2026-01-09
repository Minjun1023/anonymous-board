package com.example.anonymous_board.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    // 메인 페이지
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // 게시글 목록
    @GetMapping("/posts")
    public String showPostList() {
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