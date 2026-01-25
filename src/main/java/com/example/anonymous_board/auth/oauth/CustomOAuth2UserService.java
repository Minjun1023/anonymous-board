package com.example.anonymous_board.auth.oauth;

import com.example.anonymous_board.domain.Role;
import com.example.anonymous_board.domain.Member;
import com.example.anonymous_board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    // OAuth2UserRequest를 받아 OAuth2User를 반환
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName,
                oAuth2User.getAttributes());

        Member user = saveOrUpdate(attributes);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().getKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }

    private Member saveOrUpdate(OAuthAttributes attributes) {
        // 1. 이메일이 있는 경우 이메일로 사용자 조회
        if (attributes.getEmail() != null) {
            Optional<Member> userOptional = userRepository.findByEmail(attributes.getEmail());
            if (userOptional.isPresent()) {
                return userOptional.get();
            }
        }

        // 2. 이메일이 없거나 새로운 사용자인 경우, 닉네임과 프로바이더로 사용자 조회
        Optional<Member> userOptional = userRepository.findByNicknameAndProvider(attributes.getName(),
                attributes.getProvider());
        if (userOptional.isPresent()) {
            return userOptional.get(); // 기존 사용자 반환
        }

        // 3. 완전히 새로운 사용자일 경우, 닉네임 중복 검사 후 생성
        String nickname = attributes.getName();
        // 소셜로그인으로 가입헀을 때, 소셜로그인 정보의 닉네임이 이미 존재하는 경우
        if (userRepository.findByNickname(nickname).isPresent()) {
            nickname = nickname + "_" + UUID.randomUUID().toString().substring(0, 4);
        }

        Member newUser = Member.builder()
                .nickname(nickname) // 닉네임
                .email(attributes.getEmail()) // 이메일
                .password(UUID.randomUUID().toString()) // 비밀번호
                .role(Role.USER) // 비 로그인, 로그인, 관리자 구분
                .emailVerified(true) // 실제 검증된 이메일인지 확인 여부
                .provider(attributes.getProvider()) // 로그인 출처 (Google, Kakao, Naver)
                .build();

        return userRepository.save(newUser);
    }
}