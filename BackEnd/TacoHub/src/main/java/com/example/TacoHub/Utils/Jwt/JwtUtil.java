package com.example.TacoHub.Utils.Jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰의 생성 및 검증을 담당하는 유틸리티 클래스
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    
    @Getter
    private final long expiredMsAccess;

    @Getter
    private final long expiredMsRefresh;

    /**
     * JWT 유틸리티 클래스 생성자
     * 
     * @param secret JWT 서명에 사용할 비밀키
     * @param expiredMsAccess Access 토큰 만료 시간(ms)
     * @param expiredMsRefresh Refresh 토큰 만료 시간(ms)
     */
    public JwtUtil(@Value("${jwt.access.secret}") String secret,
                   @Value("${jwt.access.expiration}") long expiredMsAccess,
                   @Value("${jwt.refresh.expiration}") long expiredMsRefresh) {

        // 비밀키 생성 - HS256 알고리즘 사용
        this.secretKey = new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8), 
            Jwts.SIG.HS256.key().build().getAlgorithm()
        );
        this.expiredMsAccess = expiredMsAccess;
        this.expiredMsRefresh = expiredMsRefresh;
    }

    /**
     * JWT의 category 항목 추출
     * 
     * @param token JWT 토큰
     * @return "access" 또는 "refresh"
     */
    public String getCategory(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("category", String.class);
    }

    /**
     * 토큰 만료 여부 확인
     * 
     * @param token JWT 토큰
     * @return 만료되었으면 true, 유효하면 false
     */
    public Boolean isExpired(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration()
                    .before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * 토큰에서 사용자명(이메일) 추출
     * 
     * @param token JWT 토큰
     * @return 사용자명(이메일)
     */
    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("username", String.class);
    }

    /**
     * 토큰에서 역할(권한) 정보 추출
     * 
     * @param token JWT 토큰
     * @return 역할 정보(ROLE_USER, ROLE_ADMIN 등)
     */
    public String getRole(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    /**
     * Access Token 생성
     * 
     * @param username 사용자명(이메일)
     * @param role 역할(권한)
     * @return 생성된 JWT Access 토큰
     */
    public String createAccessJwt(String username, String role) {
        return Jwts.builder()
                .claim("category", "access")
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMsAccess))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token 생성
     * 
     * @param username 사용자명(이메일)
     * @param role 역할(권한)
     * @return 생성된 JWT Refresh 토큰
     */
    public String createRefreshJwt(String username, String role) {
        return Jwts.builder()
                .claim("category", "refresh")
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMsRefresh))
                .signWith(secretKey)
                .compact();
    }
}
