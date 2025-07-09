package com.example.TacoHub.Service;

import com.example.TacoHub.Dto.EmailVerificationDto;
import com.example.TacoHub.Exception.AuthCodeOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Random;

/**
 * 인증 코드 생성, 검증, 저장 및 삭제 기능을 제공하는 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthCodeService {

    private final RedisService<String> redisService;

    @Value("${spring.mail.auth-code-expiration}")
    private long authCodeExpirationMillis;

    /**
     * 6자리 숫자로 이루어진 안전한 랜덤 인증 코드를 생성한다.
     *
     * @return 생성된 6자리 인증 코드
     * @throws AuthCodeOperationException 보안 랜덤 알고리즘을 사용할 수 없는 경우 발생
     */
    public String createAuthCode()
    {
        try
        {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                builder.append(random.nextInt(10));
            }
            return builder.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            handleAndThrowAuthCodeException("createAuthCode", e);
            return null; // 실제로는 도달하지 않음
        }
    }

    /**
     * 사용자가 제공한 인증 코드의 유효성을 검증한다.
     *
     * @param emailVerificationDto 이메일, 인증 코드, 인증 목적을 포함하는 DTO
     * @return 인증 성공 여부 (true: 성공, false: 실패)
     * @throws AuthCodeOperationException 검증 과정에서 시스템 오류가 발생할 경우
     */
    public boolean verifyAuthCode(EmailVerificationDto emailVerificationDto) {
        try {
            // 입력값 검증
            if (emailVerificationDto == null) {
                log.warn("인증 정보가 비어있음");
                throw new AuthCodeOperationException("인증 정보는 필수입니다");
            }
            
            String email = emailVerificationDto.getEmail();
            String authCode = emailVerificationDto.getAuthCode();
            String purpose = emailVerificationDto.getPurpose();

            if (email == null || email.trim().isEmpty()) {
                log.warn("이메일이 비어있음");
                throw new AuthCodeOperationException("이메일은 필수입니다");
            }
            if (authCode == null || authCode.trim().isEmpty()) {
                log.warn("인증 코드가 비어있음");
                throw new AuthCodeOperationException("인증 코드는 필수입니다");
            }
            if (purpose == null || purpose.trim().isEmpty()) {
                log.warn("인증 목적이 비어있음");
                throw new AuthCodeOperationException("인증 목적은 필수입니다");
            }

            String key = getKey(email, purpose);
            String savedAuthCode = redisService.getValues(key);

            if (savedAuthCode != null && savedAuthCode.equals(authCode)) {
                return true;
            }
            return false;

        } catch (AuthCodeOperationException e) {
            log.warn("인증 코드 검증 비즈니스 오류: {}", e.getMessage());
            throw e; // 비즈니스 예외는 그대로 전파
        } catch (Exception e) {
            handleAndThrowAuthCodeException("verifyAuthCode", e);
            return false; // 실제로는 도달하지 않음
        }
    }

    /**
     * Redis에 인증 코드를 저장한다.
     *
     * @param email 인증 코드와 연결될 이메일 주소
     * @param authCode 저장할 인증 코드
     * @param purpose 인증 목적 (예: 회원가입, 비밀번호 재설정)
     * @throws AuthCodeOperationException Redis 저장 과정에서 오류가 발생할 경우
     */
    public void setAuthCodeInRedis(String email, String authCode, String purpose) {
        try {
            // 입력값 검증
            if (email == null || email.trim().isEmpty()) {
                log.warn("이메일이 비어있음");
                throw new AuthCodeOperationException("이메일은 필수입니다");
            }
            if (authCode == null || authCode.trim().isEmpty()) {
                log.warn("인증 코드가 비어있음");
                throw new AuthCodeOperationException("인증 코드는 필수입니다");
            }
            if (purpose == null || purpose.trim().isEmpty()) {
                log.warn("인증 목적이 비어있음");
                throw new AuthCodeOperationException("인증 목적은 필수입니다");
            }

            String key = getKey(email, purpose);
            Duration authCodeExpiration = Duration.ofMillis(authCodeExpirationMillis);
            redisService.setValues(key, authCode, authCodeExpiration);
        } catch (AuthCodeOperationException e) {
            log.warn("인증 코드 저장 비즈니스 오류: {}", e.getMessage());
            throw e; // 비즈니스 예외는 그대로 전파
        } catch (Exception e) {
            handleAndThrowAuthCodeException("setAuthCodeInRedis", e);
        }
    }

    /**
     * Redis에서 인증 코드를 삭제한다.
     *
     * @param email 인증 코드와 연결된 이메일 주소
     * @param purpose 인증 목적
     * @throws AuthCodeOperationException Redis 삭제 과정에서 오류가 발생할 경우
     */
    public void deleteAuthCodeInRedis(String email, String purpose) {
        try {
            // 입력값 검증
            if (email == null || email.trim().isEmpty()) {
                log.warn("이메일이 비어있음");
                throw new AuthCodeOperationException("이메일은 필수입니다");
            }
            if (purpose == null || purpose.trim().isEmpty()) {
                log.warn("인증 목적이 비어있음");
                throw new AuthCodeOperationException("인증 목적은 필수입니다");
            }

            String key = getKey(email, purpose);
            redisService.deleteValues(key);
        } catch (AuthCodeOperationException e) {
            log.warn("인증 코드 삭제 비즈니스 오류: {}", e.getMessage());
            throw e; // 비즈니스 예외는 그대로 전파
        } catch (Exception e) {
            handleAndThrowAuthCodeException("deleteAuthCodeInRedis", e);
        }
    }

    /**
     * Redis에 저장될 키를 생성한다.
     *
     * @param email 이메일 주소
     * @param purpose 인증 목적
     * @return 이메일과 목적을 조합한 키 문자열
     */
    private String getKey(String email, String purpose) {
        return email + "_" + purpose;
    }

    /**
     * 공통 AuthCode 예외 처리 메서드
     * @param methodName 실패한 메서드명
     * @param originalException 원본 예외
     * @throws AuthCodeOperationException 래핑된 예외
     */
    private void handleAndThrowAuthCodeException(String methodName, Exception originalException) {
        String errorMessage = originalException.getMessage();
        String exceptionType = originalException.getClass().getSimpleName();
        
        log.error("{} 실패: type={}, message={}", methodName, exceptionType, errorMessage, originalException);
        
        throw new AuthCodeOperationException(
            String.format("%s 실패 [%s]: %s", methodName, exceptionType, errorMessage),
            originalException
        );
    }

}
