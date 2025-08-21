package com.example.TacoHub.Utils.Jwt;

import com.example.TacoHub.Dto.ErrorResponseDTO;
import com.example.TacoHub.Entity.AccountEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * JWT 토큰을 검증하는 필터
 * 모든 요청에 대해 한 번만 실행되며, 요청 헤더에서 access 토큰을 추출하여 유효성을 검증함
 */
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private static final String ACCESS_TOKEN_HEADER = "access";
    private static final String ACCESS_CATEGORY = "access";

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 헤더에서 access 토큰 추출
        String accessToken = request.getHeader(ACCESS_TOKEN_HEADER);

        // 토큰이 없으면 다음 필터로 진행
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 만료 확인
        if (jwtUtil.isExpired(accessToken)) {
            sendErrorResponse(response, "access token expired", HttpServletResponse.SC_NOT_ACCEPTABLE);
            return;
        }

        // 토큰 카테고리 확인
        String category = jwtUtil.getCategory(accessToken);
        if (!ACCESS_CATEGORY.equals(category)) {
            sendErrorResponse(response, "invalid access token", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰에서 사용자 정보 추출 및 인증 객체 생성
        Authentication authentication = createAuthenticationFromToken(accessToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * 오류 응답 전송
     */
    private void sendErrorResponse(HttpServletResponse response, String message, int status) throws IOException {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("ACCESS_TOKEN_ERROR", message);
        response.setStatus(status);
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        writer.print(new ObjectMapper().writeValueAsString(errorResponse));
    }

    /**
     * JWT 토큰에서 인증 객체 생성
     */
    private Authentication createAuthenticationFromToken(String token) {
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        // 계정 엔티티 생성
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setEmailId(username);
        accountEntity.setRole(role);

        // 사용자 상세 정보 생성
        CustomUserDetails userDetails = new CustomUserDetails(accountEntity);
        
        // 인증 객체 생성 및 반환
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }
}
