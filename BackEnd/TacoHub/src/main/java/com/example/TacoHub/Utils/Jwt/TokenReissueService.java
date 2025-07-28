package com.example.TacoHub.Utils.Jwt;

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

    public ResponseEntity<?> reissueRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {
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
                return new ResponseEntity<>("Cookie Error getting refresh token", HttpStatus.BAD_REQUEST);
            }

            if (refresh == null) {
                return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
            }
            if (jwtUtil.isExpired(refresh)) {
                return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
            }
            String category = jwtUtil.getCategory(refresh);
            if (!"refresh".equals(category)) {
                return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
            }
            String username = jwtUtil.getUsername(refresh);
            String storedRefresh = redisService.getValues(refreshString + username);
            if (storedRefresh == null || !storedRefresh.equals(refresh)) {
                return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
            }
            String role;
            String newAccess;
            String newRefresh;
            try {
                role = jwtUtil.getRole(refresh);
                newAccess = jwtUtil.createAccessJwt(username, role);
                newRefresh = jwtUtil.createRefreshJwt(username, role);
            } catch(Exception e) {
                return new ResponseEntity<>("JwtUtils Error, reissuing token", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            try {
                redisService.deleteValues(refreshString + username);
                addRefreshEntity(username, newRefresh);
            } catch (Exception e) {
                return new ResponseEntity<>("Redis Error, reissuing token", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            response.setHeader("access", newAccess);
            response.addCookie(createCookie("refresh", newRefresh));
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error reissuing refresh token: {}", e.getMessage(), e);
            return new ResponseEntity<>("Error reissuing refresh token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

    private void addRefreshEntity(String username, String refresh) {
        Duration expiration = Duration.ofMillis(jwtUtil.getExpiredMsRefresh());
        redisService.setValues("refresh_" + username, refresh, expiration);
    }
}
