package com.example.TacoHub.Service;

import com.example.TacoHub.Dto.EmailVerificationDto;
import com.example.TacoHub.Exception.AuthCodeOperationException;
import com.example.TacoHub.Exception.BusinessException;
import com.example.TacoHub.Logging.AuditLogging;
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
public class AuthCodeService extends BaseService {

    private final RedisService<String> redisService;

    @Value("${spring.mail.auth-code-expiration}")
    private long authCodeExpirationMillis;

    // ========== 공통 검증 메서드 ==========
    
    /**
     * 이메일 유효성 검증
     * @param email 검증할 이메일
     * @param paramName 매개변수명 (로그용)
     */
    private void validateEmail(String email, String paramName) {
        if (isStringNullOrEmpty(email)) {
            throw new AuthCodeOperationException(paramName + "은(는) 필수 입력 항목입니다. 이메일 주소를 입력해주세요.");
        }
        if (isStringTooLong(email, 255)) {
            throw new AuthCodeOperationException(paramName + "은(는) 255자를 초과할 수 없습니다. 현재 길이: " + email.length() + "자");
        }
        // 기본적인 이메일 형식 검증
        if (!email.contains("@")) {
            throw new AuthCodeOperationException(paramName + "의 형식이 올바르지 않습니다. 올바른 이메일 주소를 입력해주세요.");
        }
    }

    /**
     * 인증 코드 유효성 검증
     * @param authCode 검증할 인증 코드
     * @param paramName 매개변수명 (로그용)
     */
    private void validateAuthCode(String authCode, String paramName) {
        if (isStringNullOrEmpty(authCode)) {
            throw new AuthCodeOperationException(paramName + "은(는) 필수 입력 항목입니다. 인증 코드를 입력해주세요.");
        }
        if (authCode.length() != 6) {
            throw new AuthCodeOperationException(paramName + "은(는) 6자리 숫자여야 합니다. 현재 길이: " + authCode.length() + "자");
        }
        // 숫자만 포함하는지 검증
        if (!authCode.matches("\\d{6}")) {
            throw new AuthCodeOperationException(paramName + "은(는) 숫자만 입력 가능합니다. 6자리 숫자를 입력해주세요.");
        }
    }

    /**
     * 인증 목적 유효성 검증
     * @param purpose 검증할 인증 목적
     * @param paramName 매개변수명 (로그용)
     */
    private void validatePurpose(String purpose, String paramName) {
        if (isStringNullOrEmpty(purpose)) {
            throw new AuthCodeOperationException(paramName + "은(는) 필수 입력 항목입니다. 인증 목적을 지정해주세요.");
        }
        // 허용된 인증 목적인지 검증
        if (!purpose.equals("회원가입") && !purpose.equals("비밀번호재설정") && !purpose.equals("이메일변경")) {
            throw new AuthCodeOperationException(paramName + "이(가) 유효하지 않습니다. 허용된 인증 목적: 회원가입, 비밀번호재설정, 이메일변경");
        }
    }

    /**
     * EmailVerificationDto 유효성 검증
     * @param emailVerificationDto 검증할 이메일 검증 DTO
     * @param paramName 매개변수명 (로그용)
     */
    private void validateEmailVerificationDto(EmailVerificationDto emailVerificationDto, String paramName) {
        if (isNull(emailVerificationDto)) {
            throw new AuthCodeOperationException(paramName + "은(는) 필수 입력 항목입니다. 이메일 인증 정보를 입력해주세요.");
        }
        validateEmail(emailVerificationDto.getEmail(), "이메일");
        validateAuthCode(emailVerificationDto.getAuthCode(), "인증 코드");
        validatePurpose(emailVerificationDto.getPurpose(), "인증 목적");
    }

    /**
     * 6자리 숫자로 이루어진 안전한 랜덤 인증 코드를 생성한다.
     *
     * @return 생성된 6자리 인증 코드
     * @throws AuthCodeOperationException 보안 랜덤 알고리즘을 사용할 수 없는 경우 발생
     */
    public String createAuthCode() {
        String methodName = "createAuthCode";
        log.debug("[{}] 인증 코드 생성 시작", methodName);
        
        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                builder.append(random.nextInt(10));
            }
            String authCode = builder.toString();
            
            log.debug("[{}] 인증 코드 생성 완료", methodName);
            return authCode;
            
        } catch (NoSuchAlgorithmException e) {
            // 시스템 예외: JVM/플랫폼 레벨의 문제로 시스템 예외로 처리
            handleAndThrowAuthCodeException(methodName, e);
            return null; // 실제로는 도달하지 않음
        } catch (Exception e) {
            handleAndThrowAuthCodeException(methodName, e);
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
    @AuditLogging(action = "인증_코드_검증", includeParameters = true, includeReturnValue = true, includePerformance = true)
    public boolean verifyAuthCode(EmailVerificationDto emailVerificationDto) {
        String methodName = "verifyAuthCode";
        log.info("[{}] 인증 코드 검증 시작: email={}", methodName, 
                emailVerificationDto != null ? emailVerificationDto.getEmail() : null);
        
        try {
            // 1. 입력값 검증
            validateEmailVerificationDto(emailVerificationDto, "인증 정보");

            // 2. Redis에서 저장된 인증 코드 조회
            String email = emailVerificationDto.getEmail();
            String authCode = emailVerificationDto.getAuthCode();
            String purpose = emailVerificationDto.getPurpose();
            
            String key = getKey(email, purpose);
            String savedAuthCode = redisService.getValues(key);

            // 3. 인증 코드 비교
            boolean isValid = savedAuthCode != null && savedAuthCode.equals(authCode);
            
            log.info("[{}] 인증 코드 검증 완료: email={}, isValid={}", methodName, email, isValid);
            return isValid;

        } catch (AuthCodeOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowAuthCodeException(methodName, e);
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
    @AuditLogging(action = "인증_코드_발송", includeParameters = true, includePerformance = true)
    public void setAuthCodeInRedis(String email, String authCode, String purpose) {
        String methodName = "setAuthCodeInRedis";
        log.info("[{}] 인증 코드 저장 시작: email={}, purpose={}", methodName, email, purpose);
        
        try {
            // 1. 입력값 검증
            validateEmail(email, "이메일");
            validateAuthCode(authCode, "인증 코드");
            validatePurpose(purpose, "인증 목적");

            // 2. Redis에 저장
            String key = getKey(email, purpose);
            Duration authCodeExpiration = Duration.ofMillis(authCodeExpirationMillis);
            redisService.setValues(key, authCode, authCodeExpiration);
            
            log.info("[{}] 인증 코드 저장 완료: email={}, purpose={}", methodName, email, purpose);
            
        } catch (AuthCodeOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowAuthCodeException(methodName, e);
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
        String methodName = "deleteAuthCodeInRedis";
        log.info("[{}] 인증 코드 삭제 시작: email={}, purpose={}", methodName, email, purpose);
        
        try {
            // 1. 입력값 검증
            validateEmail(email, "이메일");
            validatePurpose(purpose, "인증 목적");

            // 2. Redis에서 삭제
            String key = getKey(email, purpose);
            redisService.deleteValues(key);
            
            log.info("[{}] 인증 코드 삭제 완료: email={}, purpose={}", methodName, email, purpose);
            
        } catch (AuthCodeOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowAuthCodeException(methodName, e);
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
