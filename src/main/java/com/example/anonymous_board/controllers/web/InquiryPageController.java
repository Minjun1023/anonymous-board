package com.example.anonymous_board.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InquiryPageController {

    @GetMapping("/my-inquiries")
    public String myInquiriesPage() {
        return "my-inquiries";
    }
}
