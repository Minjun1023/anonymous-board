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

import java.util.Optional;

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

        // DB에서 최신 사용자 정보 조회 (캐싱된 정보 대신)
        Member currentUser = userRepository.findById(member.getId())
                .orElse(member);

        Optional<Member> receiverOpt = userRepository.findById(receiverId);

        if (receiverOpt.isEmpty()) {
            // 탈퇴한 회원
            model.addAttribute("receiverDeleted", true);
            model.addAttribute("receiverNickname", "탈퇴한 회원");
            model.addAttribute("receiverProfileImage", "/profiles/default_profile.png");
        } else {
            Member receiver = receiverOpt.get();
            model.addAttribute("receiverDeleted", false);
            model.addAttribute("receiver", receiver);
            model.addAttribute("receiverNickname", receiver.getNickname());
            String profileImage = receiver.getProfileImage();
            if (profileImage != null && !profileImage.startsWith("/profiles/") && !profileImage.startsWith("http")) {
                profileImage = "/profiles/" + profileImage;
            } else if (profileImage == null) {
                profileImage = "/profiles/default_profile.png";
            }
            model.addAttribute("receiverProfileImage", profileImage);
        }

        model.addAttribute("receiverId", receiverId);
        model.addAttribute("sender", currentUser);

        // sender 프로필 이미지 처리 (최신 DB 정보 사용)
        String senderProfileImage = currentUser.getProfileImage();
        if (senderProfileImage != null && !senderProfileImage.startsWith("/profiles/")
                && !senderProfileImage.startsWith("http")) {
            senderProfileImage = "/profiles/" + senderProfileImage;
        } else if (senderProfileImage == null) {
            senderProfileImage = "/profiles/default_profile.png";
        }
        model.addAttribute("senderProfileImage", senderProfileImage);

        return "chat";
    }
}
