package com.example.TacoHub.Utils.Jwt;

import com.example.TacoHub.Dto.LogInDto;
import com.example.TacoHub.Exception.InvalidLoginRequestException;
import com.example.TacoHub.Exception.TechnicalException;
import com.example.TacoHub.Service.RedisService;
import com.example.TacoHub.Utils.Validator.InputValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;

/**
 * 로그인 요청을 처리하는 필터
 * /login 경로로 POST 요청 시 이 필터가 요청을 가로채 인증을 처리함
 * 인증 성공 시 JWT 토큰 발급
 */
@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final RedisService<String> redisService;
    private final JwtUtil jwtUtil;

    // 상수 정의
    private static final String REFRESH_PREFIX = "refresh_";
    private static final int REFRESH_EXPIRE_TIME = 60 * 60 * 24; // 1일
    private static final int COOKIE_EXPIRE_TIME = 60 * 60 * 24; // 1일
    private static final String CONTENT_TYPE = "application/json";
    private static final String LOGIN_SUCCESS_MESSAGE = "{\"message\": \"Login successful\"}";
    private static final String LOGIN_FAILURE_MESSAGE = "{\"message\": \"Unauthorized\"}";

    public LoginFilter(AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil,
                       RedisService<String> redisService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.redisService = redisService;
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException {

        try {
            // 요청에서 로그인 정보를 JSON으로 추출
            LogInDto logInDto = extractLoginDtoFromRequest(request);
            
            // 입력 유효성 검사
            validateLoginInput(logInDto.getEmailId(), logInDto.getPassword());

            // 인증 토큰 생성 후 인증 시도
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(logInDto.getEmailId(), logInDto.getPassword(), null);
            
            return authenticationManager.authenticate(authToken);
            
        } catch (InvalidLoginRequestException e) {
            throw e;
        } catch (IOException e) {
            log.error("로그인 요청 처리 중 I/O 오류 발생", e);
            throw new TechnicalException("로그인 요청을 처리하는 중 시스템 오류가 발생했습니다", e);
        } catch (Exception e) {
            log.error("로그인 처리 중 예상치 못한 오류 발생", e);
            throw new TechnicalException("로그인 처리 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 요청에서 로그인 정보를 추출
     */
    private LogInDto extractLoginDtoFromRequest(HttpServletRequest request) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ServletInputStream inputStream = request.getInputStream();
        
        if (inputStream.available() <= 0) {
            throw new InvalidLoginRequestException("로그인 요청 본문이 비어있습니다");
        }
        
        String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        return objectMapper.readValue(messageBody, LogInDto.class);
    }
    
    /**
     * 로그인 입력값 유효성 검증
     */
    private void validateLoginInput(String username, String password) {
        if (!InputValidator.isValid(username) || !InputValidator.isPasswordValid(password)) {
            throw new InvalidLoginRequestException("유효하지 않은 이메일 또는 비밀번호 형식입니다");
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authentication) throws IOException {

        // 사용자 정보 추출
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        // 권한 정보 추출
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String role = authorities.iterator().next().getAuthority();

        // JWT 토큰 생성
        String accessToken = jwtUtil.createAccessJwt(username, role);
        String refreshToken = jwtUtil.createRefreshJwt(username, role);
        
        // Redis에 Refresh 토큰 저장
        storeRefreshToken(username, refreshToken);

        // 응답 헤더 및 쿠키 설정
        response.setHeader("access", accessToken);
        response.addCookie(createCookie("refresh", refreshToken));
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(CONTENT_TYPE);
        response.getWriter().write(LOGIN_SUCCESS_MESSAGE);
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed) throws IOException {
        
        log.info("로그인 실패: IP={}", request.getRemoteAddr());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(CONTENT_TYPE);
        response.getWriter().write(LOGIN_FAILURE_MESSAGE);
    }

    /**
     * HTTP 쿠키 생성
     */
    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_EXPIRE_TIME);
        cookie.setHttpOnly(true);
        return cookie;
    }

    /**
     * Redis에 Refresh 토큰 저장
     */
    private void storeRefreshToken(String username, String refreshToken) {
        try {
            Duration expireTime = Duration.ofSeconds(REFRESH_EXPIRE_TIME);
            redisService.setValues(REFRESH_PREFIX + username, refreshToken, expireTime);
        } catch (Exception e) {
            throw new TechnicalException("Refresh 토큰 저장 중 오류가 발생했습니다", e);
        }
    }
}
