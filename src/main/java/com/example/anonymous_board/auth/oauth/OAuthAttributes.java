package com.example.anonymous_board.auth.oauth;

import com.example.anonymous_board.domain.Role;
import com.example.anonymous_board.domain.Member;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
public class OAuthAttributes {

    private Map<String, Object> attributes; // OAuth 제공자로부터 받은 사용자 정보 전체
    private String nameAttributeKey; // OAuth 식별자 키
    private String name; // 사용자 이름 또는 닉네임
    private String email; // 사용자 이메일
    private String provider; // 제공자 이름(Google, Kakao, Naver)

    // 소셜 로그인을 통해 얻은 정보
    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email,
            String provider) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.provider = provider;
    }

    // 로그인 요청 시 어떤 OAuth 제공자인지 구별
    public static OAuthAttributes of(String registrationId, String userNameAttributeName,
            Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return ofNaver(registrationId, "id", attributes);
        }
        if ("kakao".equals(registrationId)) {
            return ofKakao(registrationId, userNameAttributeName, attributes);
        }
        return ofGoogle(registrationId, userNameAttributeName, attributes);
    }

    // Google
    private static OAuthAttributes ofGoogle(String registrationId, String userNameAttributeName,
            Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .provider(registrationId)
                .build();
    }

    // Kakao
    private static OAuthAttributes ofKakao(String registrationId, String userNameAttributeName,
            Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuthAttributes.builder()
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .name((String) kakaoProfile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .provider(registrationId)
                .build();
    }

    // Naver
    private static OAuthAttributes ofNaver(String registrationId, String userNameAttributeName,
            Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuthAttributes.builder()
                .attributes(response)
                .nameAttributeKey(userNameAttributeName)
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .provider(registrationId)
                .build();
    }

    // OAuthAttributes -> User
    public Member toEntity() {
        return Member.builder()
                .nickname(name)
                .email(email)
                .password(UUID.randomUUID().toString())
                .role(Role.USER)
                .emailVerified(true)
                .provider(provider)
                .build();
    }
}