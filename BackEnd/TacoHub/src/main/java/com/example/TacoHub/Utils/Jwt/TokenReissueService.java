package com.example.TacoHub.Utils.Jwt;

import com.example.TacoHub.Exception.BusinessException;
import com.example.TacoHub.Exception.RefreshTokenExpiredException;
import com.example.TacoHub.Exception.TokenCategoryException;
import com.example.TacoHub.Exception.TokenNullException;
import com.example.TacoHub.Exception.TokenParseException;
import com.example.TacoHub.Exception.TokenRedisMismatchException;
import com.example.TacoHub.Service.RedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
public class TokenReissueService {
    private final JwtUtil jwtUtil;
    private final RedisService<String> redisService;
    private final String refreshString = "refresh_";

    public TokenReissueService(JwtUtil jwtUtil, RedisService<String> redisService) {
        this.jwtUtil = jwtUtil;
        this.redisService = redisService;
    }

    /**
     * Refresh 토큰을 검증하고, 새 access/refresh 토큰을 재발급하는 메서드
     * @param request HttpServletRequest (쿠키에서 refresh 토큰 추출)
     * @param response HttpServletResponse (새 토큰 세팅)
     * @throws TokenParseException, TokenNullException, RefreshTokenExpiredException, TokenCategoryException, TokenRedisMismatchException 등
     * 동작 순서:
     * 1. 쿠키에서 refresh 토큰 추출
     * 2. refresh 토큰 null 체크 및 만료 여부 검증
     * 3. 토큰 category가 refresh인지 확인
     * 4. Redis에 저장된 refresh 토큰과 일치하는지 검증
     * 5. 토큰 정보로 새 access/refresh 토큰 생성
     * 6. Redis에 기존 refresh 삭제 후 새 refresh 저장
     * 7. 응답 헤더/쿠키에 새 토큰 세팅
     * 8. 성공/실패에 따라 적절한 응답 반환
     */
    public void reissueRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        final String methodName = "reissueRefreshToken";
        // 1. 쿠키에서 refresh 토큰 추출
        String refresh = null;
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("refresh")) {
                        refresh = cookie.getValue();
                    }
                }
            }
        } catch (Exception e) {
            log.error("[{}] 쿠키 파싱 오류: {}", methodName, e.getMessage(), e);
            throw new TokenParseException("쿠키에서 refresh token 추출 실패");
        }

        // 2. refresh 토큰 null 체크
        if (refresh == null) {
            log.warn("[{}] refresh token null", methodName);
            throw new TokenNullException("refresh token이 존재하지 않습니다");
        }
        // 2. refresh 토큰 만료 여부 검증
        if (jwtUtil.isExpired(refresh)) {
            log.warn("[{}] refresh token expired", methodName);
            throw new RefreshTokenExpiredException("refresh token 만료");
        }
        // 3. 토큰 category가 refresh인지 확인
        String category = jwtUtil.getCategory(refresh);
        if (!"refresh".equals(category)) {
            log.warn("[{}] refresh token category 불일치: {}", methodName, category);
            throw new TokenCategoryException("refresh token category 불일치");
        }
        // 4. Redis에 저장된 refresh 토큰과 일치하는지 검증
        String username = jwtUtil.getUsername(refresh);
        String storedRefresh = redisService.getValues(refreshString + username);
        if (storedRefresh == null || !storedRefresh.equals(refresh)) {
            log.warn("[{}] refresh token redis 불일치: username={}, storedRefresh={}, requestRefresh={}", methodName, username, storedRefresh, refresh);
            throw new TokenRedisMismatchException("refresh token이 서버와 일치하지 않습니다");
        }
        // 5. 토큰 정보로 새 access/refresh 토큰 생성
        String role;
        String newAccess;
        String newRefresh;
        try {
            role = jwtUtil.getRole(refresh);
            newAccess = jwtUtil.createAccessJwt(username, role);
            newRefresh = jwtUtil.createRefreshJwt(username, role);
        } catch(BusinessException e) {
            log.warn("[{}] 토큰 생성 비즈니스 예외: {}", methodName, e.getMessage(), e);
            throw new TokenParseException("JwtUtils 비즈니스 오류: " + e.getMessage());
        } catch(Exception e) {
            log.error("[{}] 토큰 생성 시스템 오류: {}", methodName, e.getMessage(), e);
            throw new TokenParseException("JwtUtils 시스템 오류: " + e.getMessage());
        }
        // 6. Redis에 기존 refresh 삭제 후 새 refresh 저장
        try {
            redisService.deleteValues(refreshString + username);
            try {
                addRefreshEntity(username, newRefresh);
            } catch (BusinessException e) {
                log.warn("[{}] Redis 저장 비즈니스 예외: {}", methodName, e.getMessage(), e);
                throw new TokenRedisMismatchException("Redis 저장 비즈니스 오류: " + e.getMessage());
            } catch (Exception e) {
                log.error("[{}] Redis 저장 시스템 오류: {}", methodName, e.getMessage(), e);
                throw new TokenParseException("Redis 저장 시스템 오류: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("[{}] Redis 삭제 시스템 오류: {}", methodName, e.getMessage(), e);
            throw new TokenParseException("Redis 삭제 시스템 오류: " + e.getMessage());
        }
        // 7. 응답 헤더/쿠키에 새 토큰 세팅
        response.setHeader("access", newAccess);
        response.addCookie(createCookie("refresh", newRefresh));
        // 8. 성공: 별도 응답 반환 필요 없음 (GlobalExceptionHandler에서 처리)
    }
    

    /**
     * 새 refresh 토큰을 Redis에 저장하는 메서드
     * @param username 사용자명
     * @param refresh 새 refresh 토큰
     * @throws TokenRedisMismatchException, TokenParseException 등
     */
    private void addRefreshEntity(String username, String refresh) {
        final String methodName = "addRefreshEntity";
        try {
            Duration expiration = Duration.ofMillis(jwtUtil.getExpiredMsRefresh());
            redisService.setValues("refresh_" + username, refresh, expiration);
        } catch (BusinessException e) {
            log.warn("[{}] Redis 저장 비즈니스 예외: {}", methodName, e.getMessage(), e);
            throw new TokenRedisMismatchException("Redis 저장 비즈니스 오류: " + e.getMessage());
        } catch (Exception e) {
            log.error("[{}] Redis 저장 시스템 오류: {}", methodName, e.getMessage(), e);
            throw new TokenParseException("Redis 저장 시스템 오류: " + e.getMessage());
        }
    }

    /**
     * refresh/access 토큰을 쿠키로 생성하는 메서드
     * @param key 쿠키명
     * @param value 쿠키값
     * @return 생성된 쿠키 객체
     */
    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }
}
