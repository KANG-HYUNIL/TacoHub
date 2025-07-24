package com.example.TacoHub.Service;

import com.example.TacoHub.Converter.AccountConverter;
import com.example.TacoHub.Dto.AccountDto;
import com.example.TacoHub.Dto.EmailVerificationDto;
import com.example.TacoHub.Entity.AccountEntity;
import com.example.TacoHub.Exception.AccountNotFoundException;
import com.example.TacoHub.Exception.AccountOperationException;
import com.example.TacoHub.Exception.BusinessException;
import com.example.TacoHub.Exception.EmailAlreadyExistsException;
import com.example.TacoHub.Exception.InvalidAuthCodeException;
import com.example.TacoHub.Logging.AuditLogging;
import com.example.TacoHub.Repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 사용자 계정 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService extends BaseService {

    private final AuthCodeService authCodeService;
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private final String ROLE_USER = "ROLE_USER";
    
    // ========== 공통 검증 메서드 ==========
    
    /**
     * 이메일 ID 유효성 검증
     * @param emailId 검증할 이메일 ID
     * @param paramName 매개변수명 (로그용)
     */
    private void validateEmailId(String emailId, String paramName) {
        if (isStringNullOrEmpty(emailId)) {
            throw new AccountOperationException(paramName + "은(는) 필수 입력 항목입니다. 이메일 주소를 입력해주세요.");
        }
        if (isStringTooLong(emailId, 255)) {
            throw new AccountOperationException(paramName + "은(는) 255자를 초과할 수 없습니다. 현재 길이: " + emailId.length() + "자");
        }
        // 기본적인 이메일 형식 검증 (@ 포함 여부)
        if (!emailId.contains("@")) {
            throw new AccountOperationException(paramName + "의 형식이 올바르지 않습니다. 올바른 이메일 주소를 입력해주세요.");
        }
    }

    /**
     * 비밀번호 유효성 검증
     * @param password 검증할 비밀번호
     * @param paramName 매개변수명 (로그용)
     */
    private void validatePassword(String password, String paramName) {
        if (isStringNullOrEmpty(password)) {
            throw new AccountOperationException(paramName + "은(는) 필수 입력 항목입니다. 비밀번호를 입력해주세요.");
        }
        if (isStringTooShort(password, 8)) {
            throw new AccountOperationException(paramName + "은(는) 최소 8자 이상이어야 합니다. 현재 길이: " + password.length() + "자");
        }
        if (isStringTooLong(password, 100)) {
            throw new AccountOperationException(paramName + "은(는) 100자를 초과할 수 없습니다. 현재 길이: " + password.length() + "자");
        }
    }

    /**
     * 인증 코드 유효성 검증
     * @param authCode 검증할 인증 코드
     * @param paramName 매개변수명 (로그용)
     */
    private void validateAuthCode(String authCode, String paramName) {
        if (isStringNullOrEmpty(authCode)) {
            throw new AccountOperationException(paramName + "은(는) 필수 입력 항목입니다. 이메일로 전송된 인증 코드를 입력해주세요.");
        }
        if (authCode.length() != 6) {
            throw new AccountOperationException(paramName + "은(는) 6자리 숫자여야 합니다. 현재 길이: " + authCode.length() + "자");
        }
        // 숫자만 포함하는지 검증
        if (!authCode.matches("\\d{6}")) {
            throw new AccountOperationException(paramName + "은(는) 숫자만 입력 가능합니다. 이메일로 전송된 6자리 숫자를 입력해주세요.");
        }
    }

    /**
     * 인증 목적 유효성 검증
     * @param purpose 검증할 인증 목적
     * @param paramName 매개변수명 (로그용)
     */
    private void validatePurpose(String purpose, String paramName) {
        if (isStringNullOrEmpty(purpose)) {
            throw new AccountOperationException(paramName + "은(는) 필수 입력 항목입니다. 인증 목적을 지정해주세요.");
        }
    }

    /**
     * AccountDto 유효성 검증
     * @param accountDto 검증할 계정 DTO
     * @param paramName 매개변수명 (로그용)
     */
    private void validateAccountDto(AccountDto accountDto, String paramName) {
        if (isNull(accountDto)) {
            throw new AccountOperationException(paramName + "은(는) 필수 입력 항목입니다. 계정 정보를 입력해주세요.");
        }
        validateEmailId(accountDto.getEmailId(), "이메일 ID");
        validatePassword(accountDto.getPassword(), "비밀번호");
    }
    /**
     * 이메일 ID 중복 검사를 수행한다.
     * 
     * @param emailId 중복 검사할 이메일 ID
     * @return 이메일이 이미 존재하면 true, 존재하지 않으면 false 반환
     */
    @AuditLogging(action = "이메일_중복_확인", includeParameters = true, includeReturnValue = true)
    public boolean checkEmailId(String emailId) {
        String methodName = "checkEmailId";
        
        try {
            validateEmailId(emailId, "이메일 ID");
            return accountRepository.existsByEmailId(emailId);
            
        } catch (AccountOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowAccountException(methodName, e);
            return false; // 실제로는 도달하지 않음
        }
    }

    /**
     * 이메일 존재 여부 확인 (초대 시스템용)
     * 
     * @param email 확인할 이메일 주소
     * @return 이메일이 존재하면 true, 존재하지 않으면 false 반환
     */
    @AuditLogging(action = "사용자_존재_확인", includeParameters = true, includeReturnValue = true)
    public boolean existsByEmail(String email) {
        String methodName = "existsByEmail";
        
        try {
            validateEmailId(email, "이메일");
            return accountRepository.existsByEmailId(email);
            
        } catch (AccountOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowAccountException(methodName, e);
            return false; // 실제로는 도달하지 않음
        }
    }

    /**
     * 사용자 회원가입을 처리한다.
     * 
     * @param accountDto 회원가입에 필요한 사용자 정보를 담은 DTO
     * @param authCode 이메일 인증 코드
     * @param purpose 인증 목적 (예: 회원가입)
     * @throws EmailAlreadyExistsException 이미 존재하는 이메일인 경우 발생
     * @throws InvalidAuthCodeException 인증 코드가 유효하지 않은 경우 발생
     */
    @AuditLogging(action = "회원가입", includeParameters = true, includePerformance = true)
    public void signUp(AccountDto accountDto, String authCode, String purpose) {
        String methodName = "signUp";
        log.info("[{}] 회원가입 시작: emailId={}", methodName, accountDto != null ? accountDto.getEmailId() : null);
        
        try {
            // 1. 입력값 검증
            validateAccountDto(accountDto, "계정 정보");
            validateAuthCode(authCode, "인증 코드");
            validatePurpose(purpose, "인증 목적");

            // 2. 이메일 ID 중복 재검사(클라이언트를 믿지마)
            if (checkEmailId(accountDto.getEmailId())) {
                throw new EmailAlreadyExistsException("이미 존재하는 이메일입니다");
            }

            // 3. 인증 코드 검증
            String email = accountDto.getEmailId();
            EmailVerificationDto emailVerificationDto = new EmailVerificationDto(email, authCode, purpose);
            
            if (!authCodeService.verifyAuthCode(emailVerificationDto)) {
                throw new InvalidAuthCodeException("인증 코드가 유효하지 않습니다");
            }

            // 4. 계정 정보 설정 및 저장
            accountDto.setRole(ROLE_USER); // 기본 역할 설정
            String encodedPassword = passwordEncoder.encode(accountDto.getPassword());
            accountDto.setPassword(encodedPassword);

            AccountEntity accountEntity = AccountConverter.toEntity(accountDto);
            accountRepository.save(accountEntity);
            
            log.info("[{}] 회원가입 완료: emailId={}", methodName, accountDto.getEmailId());

        } catch (EmailAlreadyExistsException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (InvalidAuthCodeException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (AccountOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowAccountException(methodName, e);
        }
    }




    /**
     * AccountEntity를 emailId로 조회하고, 없으면 예외를 던지는 메서드
     * @param emailId 조회할 사용자의 이메일 ID
     * @return 조회된 AccountEntity
     * @throws AccountNotFoundException 이메일이 존재하지 않을 때
     */
    @AuditLogging(action = "사용자_정보_조회", includeParameters = true)
    public AccountEntity getAccountEntityOrThrow(String emailId) {
        return accountRepository.findByEmailId(emailId)
                .orElseThrow(() -> {
                    log.warn("사용자 조회 실패: 이메일이 존재하지 않음, emailId={}", emailId);
                    return new AccountNotFoundException("사용자가 존재하지 않습니다: " + emailId);
                });
    }

    /**
     * 공통 Account 예외 처리 메서드
     * 예외 타입에 따라 자동으로 warn/error 로깅을 결정
     * 
     * @param methodName 실패한 메서드명
     * @param originalException 원본 예외
     * @throws AccountOperationException 래핑된 예외
     */
    private void handleAndThrowAccountException(String methodName, Exception originalException) {
        AccountOperationException customException = new AccountOperationException(
            String.format("%s 실패 [%s]: %s", methodName, 
                         originalException.getClass().getSimpleName(), 
                         originalException.getMessage()),
            originalException
        );
        
        // BaseService의 메서드를 사용하여 예외 타입에 따라 warn/error 로깅
        handleAndThrow(methodName, originalException, customException);
    }

}
