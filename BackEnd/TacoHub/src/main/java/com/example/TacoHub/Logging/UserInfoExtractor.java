package com.example.TacoHub.Logging;

import com.example.TacoHub.Utils.Jwt.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 사용자 정보 추출 유틸리티 클래스
 * SecurityContext와 HTTP 요청에서 사용자 관련 정보를 추출
 */
@Component
public class UserInfoExtractor {
    
    /**
     * 현재 인증된 사용자 ID 추출
     * @return 사용자 ID (인증되지 않은 경우 null)
     */
    public String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                return userDetails.getUsername(); // 기존 getUsername() 사용 (이메일 반환)
            }
        } catch (Exception e) {
            // 인증 정보 추출 실패 시 null 반환 (로그는 남기지 않음 - 너무 빈번할 수 있음)
        }
        return null;
    }
    
    /**
     * 현재 인증된 사용자 이메일 추출
     * @return 사용자 이메일 (인증되지 않은 경우 null)
     */
    public String getCurrentUserEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                return userDetails.getUsername(); // 이메일이 username으로 저장됨
            }
        } catch (Exception e) {
            // 인증 정보 추출 실패 시 null 반환
        }
        return null;
    }
    
    /**
     * 현재 인증된 사용자 권한 추출
     * @return 사용자 권한 (인증되지 않은 경우 null)
     */
    public String getCurrentUserRole() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                // 권한이 여러 개일 수 있지만, 첫 번째 권한만 반환
                return userDetails.getAuthorities().isEmpty() ? null : 
                       userDetails.getAuthorities().iterator().next().getAuthority();
            }
        } catch (Exception e) {
            // 인증 정보 추출 실패 시 null 반환
        }
        return null;
    }
    
    /**
     * 클라이언트 IP 주소 추출
     * 프록시나 로드밸런서를 고려하여 실제 클라이언트 IP 추출
     * @return 클라이언트 IP 주소
     */
    public String getClientIpAddress() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) return null;
            
            // 프록시 환경에서 실제 클라이언트 IP 추출 (우선순위 순서)
            String[] headerNames = {
                "X-Forwarded-For",        // 가장 표준적인 프록시 헤더 (RFC 7239)
                "X-Real-IP",              // Nginx에서 주로 사용
                "CF-Connecting-IP",       // Cloudflare에서 사용
                "True-Client-IP",         // Akamai, CloudFlare에서 사용
                "X-Cluster-Client-IP",    // 클러스터 환경에서 사용
                "X-Forwarded",            // 비표준이지만 일부 프록시에서 사용
                "Forwarded-For",          // 구형 프록시에서 사용
                "Forwarded",              // RFC 7239 표준
                "Proxy-Client-IP",        // Apache mod_proxy에서 사용
                "WL-Proxy-Client-IP",     // WebLogic에서 사용
                "HTTP_X_FORWARDED_FOR",   // CGI 환경에서 사용
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
            };
            
            for (String header : headerNames) {
                String ip = request.getHeader(header);
                if (isValidIp(ip)) {
                    // X-Forwarded-For는 "client, proxy1, proxy2" 형태일 수 있음
                    // 첫 번째 IP가 실제 클라이언트 IP
                    if (ip.contains(",")) {
                        ip = ip.split(",")[0].trim();
                    }
                    // IPv6 형태에서 대괄호 제거
                    if (ip.startsWith("[") && ip.endsWith("]")) {
                        ip = ip.substring(1, ip.length() - 1);
                    }
                    return ip;
                }
            }
            
            // 모든 헤더에서 찾지 못한 경우 기본 원격 주소 반환
            String remoteAddr = request.getRemoteAddr();
            
            // 로컬 환경에서 IPv6 루프백을 IPv4로 변환
            if ("0:0:0:0:0:0:0:1".equals(remoteAddr) || "::1".equals(remoteAddr)) {
                return "127.0.0.1";
            }
            
            return remoteAddr;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 유효한 IP 주소인지 확인
     * @param ip 검증할 IP 주소
     * @return 유효성 여부
     */
    private boolean isValidIp(String ip) {
        return ip != null 
            && !ip.isEmpty() 
            && !"unknown".equalsIgnoreCase(ip)
            && !"null".equalsIgnoreCase(ip)
            && !ip.trim().equals("");
    }
    
    /**
     * User-Agent 정보 추출
     * @return User-Agent 문자열
     */
    public String getUserAgent() {
        try {
            HttpServletRequest request = getCurrentRequest();
            return request != null ? request.getHeader("User-Agent") : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 세션 ID 추출
     * @return 세션 ID
     */
    public String getSessionId() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null && request.getSession(false) != null) {
                return request.getSession().getId();
            }
        } catch (Exception e) {
            // 세션 정보 추출 실패
        }
        return null;
    }
    
    /**
     * 현재 HTTP 요청 객체 가져오기
     * @return HttpServletRequest (없으면 null)
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
    
}
