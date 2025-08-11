package com.example.TacoHub.Oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.TacoHub.Utils.Jwt.JwtUtil;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    
    @Value("${app.domain.oauth2.success-redirect-url}")
    private String successRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        // Authentication 에서 사용자 정보 획득
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        // 사용자 이메일 ID 획득
        String emailId = customOAuth2User.getName();

        //사용자 Role 획득
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String role = authorities.iterator().next().getAuthority();

        // JWT Access, Refresh Token 생성
        String accessToken = jwtUtil.createAccessJwt(emailId, role);
        String refreshToken = jwtUtil.createRefreshJwt(emailId, role);

        // 생성된 토큰을 응답 본문에 추가
        response.addCookie(createCookie("access", accessToken, false));
        response.addCookie(createCookie("refresh", refreshToken, true));
        response.setStatus(HttpServletResponse.SC_OK);
        
        // 하드코딩된 프론트엔드 URL로 리다이렉트
        response.sendRedirect(successRedirectUrl);
    }



        /**
     * HTTP 쿠키 생성
     */
    private Cookie createCookie(String key, String value, boolean isSecure) {
        Cookie cookie = new Cookie(key, value);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 60);
        cookie.setHttpOnly(false);
        cookie.setSecure(isSecure);
        return cookie;
    }

}
