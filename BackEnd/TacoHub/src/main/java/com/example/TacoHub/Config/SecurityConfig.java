package com.example.TacoHub.Config;

import com.example.TacoHub.Service.RedisService;
import com.example.TacoHub.Utils.Jwt.JwtFilter;
import com.example.TacoHub.Utils.Jwt.JwtUtil;
import com.example.TacoHub.Utils.Jwt.LoginFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정 클래스
 * 인증/인가 관련 설정과 보안 필터 구성을 담당
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // 경로 상수 정의
    private static final String LOGIN_URL = "/login";
    private static final String LOGOUT_URL = "/logout";
    
    // 필요한 의존성 주입
    private final AuthenticationConfiguration authenticationConfiguration;
    private final RedisService<String> redisService;
    private final JwtUtil jwtUtil;

    /**
     * 인증 관리자 빈 등록
     * AuthenticationManager는 Spring Security 인증 처리의 핵심 인터페이스
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * 특정 경로에 대해 Spring Security를 적용하지 않도록 설정
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/h2-console/**", "/favicon.ico", "/error");
    }

    /**
     * 비밀번호 암호화를 위한 인코더 빈 등록
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 보안 필터 체인 구성
     * 인증/인가 규칙과 적용할 필터를 정의
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF 보호 비활성화 (REST API에서는 일반적으로 불필요)
        http.csrf((csrf) -> csrf.disable());

        // 폼 로그인 비활성화 (REST API 방식 사용)
        http.formLogin((form) -> form.disable());
        
        // HTTP Basic 인증 비활성화
        http.httpBasic((basic) -> basic.disable());

        // 경로별 인가 규칙 설정
        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN") // 관리자 권한 필요
                .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN") // 사용자 또는 관리자 권한 필요
                .requestMatchers("/**").permitAll() // 나머지 경로는 모든 사용자 허용
        );

        // 세션 관리 설정 - JWT 사용으로 세션 상태 유지 안함
        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // 커스텀 필터 추가
        LoginFilter loginFilter = new LoginFilter(
                authenticationManager(authenticationConfiguration), 
                jwtUtil, 
                redisService
        );
        
        // 로그인 경로 설정
        loginFilter.setFilterProcessesUrl(LOGIN_URL);

        // JWT 검증 필터 설정
        JwtFilter jwtFilter = new JwtFilter(jwtUtil);
        
        // 필터 순서 지정
        http.addFilterBefore(jwtFilter, LoginFilter.class); // JWT 검증 필터를 먼저 실행
        http.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class); // 로그인 처리 필터

        return http.build();
    }
}
