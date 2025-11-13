package com.example.anonymous_board.auth.oauth;

import com.example.anonymous_board.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
/**
 * OAuth2 로그인 과정에서 Authorization Request를 쿠키에 저장하고 관리하는 클래스
 * - OAuth2 인증 요청 정보를 서버 세션 대신 쿠키에 저장
 * - 리다이렉트 URI 등 로그인 후 처리 정보를 함께 관리
 */
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    // OAuth2 인증 요청 정보를 담는 쿠키
    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";

    // 로그인 후 리다이렉트할 URI를 담는 쿠키
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";

    // 쿠키 유효 시간
    private static final int cookieExpireSeconds = 180; 

    /**
     * 요청에서 OAuth2 인증 요청을 쿠키에서 읽어오는 메서드
     * @param request HttpServletRequest
     * @return 쿠키에 저장된 OAuth2AuthorizationRequest, 없으면 null
     */
    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(cookie -> CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest.class))
                .orElse(null);
    }

    /**
     * OAuth2 인증 요청 정보를 쿠키에 저장
     * @param authorizationRequest 인증 요청 정보
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) { // 저장할 요청 정보가 없을 경우 기존 쿠키 삭제
            CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
            return;
        }

        // OAuth2 인증 요청 정보를 쿠키에 직렬화하여 저장
        CookieUtils.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, CookieUtils.serialize(authorizationRequest), cookieExpireSeconds);

        // 로그인 후 리다이렉트 URI가 있을 경우 쿠키에 저장
        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (redirectUriAfterLogin != null && !redirectUriAfterLogin.isBlank()) {
            CookieUtils.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, cookieExpireSeconds);
        }
    }

    /**
     * 요청에서 OAuth2 인증 요청을 제거하면서 반환
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return 기존에 저장된 OAuth2AuthorizationRequest
     */
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키 삭제 없이 읽기만 실행
        return this.loadAuthorizationRequest(request);
    }

    /**
     * 쿠키에 저장된 OAuth2 인증 요청 정보 및 리다이렉트 URI 쿠키를 삭제
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }
}
