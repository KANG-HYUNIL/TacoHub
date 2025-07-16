package com.example.TacoHub.Service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.TacoHub.Dto.AccountDto;
import com.example.TacoHub.Dto.EmailVerificationDto;
import com.example.TacoHub.Entity.AccountEntity;
import com.example.TacoHub.Exception.AccountNotFoundException;
import com.example.TacoHub.Exception.AccountOperationException;
import com.example.TacoHub.Exception.EmailAlreadyExistsException;
import com.example.TacoHub.Exception.InvalidAuthCodeException;
import com.example.TacoHub.Repository.AccountRepository;

/**
 * AccountService 단위 테스트
 * Given/When/Then 패턴과 현업 수준의 테스트 케이스 작성
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService 단위 테스트")
public class AccountServiceTest {

    @Mock
    private AuthCodeService authCodeService;
    
    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    
    @InjectMocks
    private AccountService accountService;
    
    // 테스트용 상수 정의
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "password123";
    private static final String VALID_AUTH_CODE = "123456";
    private static final String SIGNUP_PURPOSE = "회원가입";
    private static final String ENCODED_PASSWORD = "encoded_password";
    private static final String ROLE_USER = "ROLE_USER";
    
    private AccountDto validAccountDto;
    private AccountEntity validAccountEntity;
    
    @BeforeEach
    void setUp() {
        // Given: 테스트용 유효한 AccountDto 준비
        validAccountDto = AccountDto.builder()
                .emailId(VALID_EMAIL)
                .password(VALID_PASSWORD)
                .name("테스트사용자")
                .build();
        
        // Given: 테스트용 유효한 AccountEntity 준비
        validAccountEntity = AccountEntity.builder()
                .emailId(VALID_EMAIL)
                .password(ENCODED_PASSWORD)
                .name("테스트사용자")
                .role(ROLE_USER)
                .build();
    }
    
    @Nested
    @DisplayName("checkEmailId 메서드 테스트")
    class CheckEmailIdTest {
        
        @Test
        @DisplayName("정상적인 이메일 ID가 주어졌을 때 중복 검사가 성공해야 한다")
        void checkEmailId_WithValidEmail_ShouldReturnTrue() {
            // Given: 유효한 이메일 ID와 DB에 해당 이메일이 존재하는 상황
            given(accountRepository.existsByEmailId(VALID_EMAIL)).willReturn(true);
            
            // When: 이메일 중복 검사 실행
            boolean result = accountService.checkEmailId(VALID_EMAIL);
            
            // Then: 중복 검사 결과가 true여야 함
            assertThat(result).isTrue();
            // Then: Repository 메서드가 정확히 한 번 호출되어야 함
            then(accountRepository).should(times(1)).existsByEmailId(VALID_EMAIL);
        }
        
        @Test
        @DisplayName("존재하지 않는 이메일 ID가 주어졌을 때 중복 검사가 false를 반환해야 한다")
        void checkEmailId_WithNonExistentEmail_ShouldReturnFalse() {
            // Given: 유효한 이메일 ID와 DB에 해당 이메일이 존재하지 않는 상황
            String nonExistentEmail = "nonexistent@example.com";
            given(accountRepository.existsByEmailId(nonExistentEmail)).willReturn(false);
            
            // When: 이메일 중복 검사 실행
            boolean result = accountService.checkEmailId(nonExistentEmail);
            
            // Then: 중복 검사 결과가 false여야 함
            assertThat(result).isFalse();
            // Then: Repository 메서드가 정확히 한 번 호출되어야 함
            then(accountRepository).should(times(1)).existsByEmailId(nonExistentEmail);
        }
        
        @Test
        @DisplayName("null 이메일 ID가 주어졌을 때 AccountOperationException이 발생해야 한다")
        void checkEmailId_WithNullEmail_ShouldThrowAccountOperationException() {
            // Given: null 이메일 ID
            String nullEmail = null;
            
            // When & Then: AccountOperationException이 발생해야 함
            assertThatThrownBy(() -> accountService.checkEmailId(nullEmail))
                    .isInstanceOf(AccountOperationException.class)
                    .hasMessageContaining("이메일 ID은(는) 필수 입력 항목입니다");
            
            // Then: Repository 메서드는 호출되지 않아야 함
            then(accountRepository).should(never()).existsByEmailId(anyString());
        }
        
        @Test
        @DisplayName("빈 문자열 이메일 ID가 주어졌을 때 AccountOperationException이 발생해야 한다")
        void checkEmailId_WithEmptyEmail_ShouldThrowAccountOperationException() {
            // Given: 빈 문자열 이메일 ID
            String emptyEmail = "";
            
            // When & Then: AccountOperationException이 발생해야 함
            assertThatThrownBy(() -> accountService.checkEmailId(emptyEmail))
                    .isInstanceOf(AccountOperationException.class)
                    .hasMessageContaining("이메일 ID은(는) 필수 입력 항목입니다");
            
            // Then: Repository 메서드는 호출되지 않아야 함
            then(accountRepository).should(never()).existsByEmailId(anyString());
        }
        
        @Test
        @DisplayName("@가 포함되지 않은 잘못된 형식의 이메일 ID가 주어졌을 때 AccountOperationException이 발생해야 한다")
        void checkEmailId_WithInvalidEmailFormat_ShouldThrowAccountOperationException() {
            // Given: 잘못된 형식의 이메일 ID (@ 없음)
            String invalidEmail = "invalidemailformat";
            
            // When & Then: AccountOperationException이 발생해야 함
            assertThatThrownBy(() -> accountService.checkEmailId(invalidEmail))
                    .isInstanceOf(AccountOperationException.class)
                    .hasMessageContaining("이메일 ID의 형식이 올바르지 않습니다");
            
            // Then: Repository 메서드는 호출되지 않아야 함
            then(accountRepository).should(never()).existsByEmailId(anyString());
        }
        
        @Test
        @DisplayName("255자를 초과하는 이메일 ID가 주어졌을 때 AccountOperationException이 발생해야 한다")
        void checkEmailId_WithTooLongEmail_ShouldThrowAccountOperationException() {
            // Given: 255자를 초과하는 이메일 ID
            String tooLongEmail = "a".repeat(250) + "@example.com"; // 263자
            
            // When & Then: AccountOperationException이 발생해야 함
            assertThatThrownBy(() -> accountService.checkEmailId(tooLongEmail))
                    .isInstanceOf(AccountOperationException.class)
                    .hasMessageContaining("이메일 ID은(는) 255자를 초과할 수 없습니다");
            
            // Then: Repository 메서드는 호출되지 않아야 함
            then(accountRepository).should(never()).existsByEmailId(anyString());
        }
    }
    
    @Nested
    @DisplayName("existsByEmail 메서드 테스트")
    class ExistsByEmailTest {
        
        @Test
        @DisplayName("존재하는 이메일이 주어졌을 때 true를 반환해야 한다")
        void existsByEmail_WithExistingEmail_ShouldReturnTrue() {
            // Given: DB에 존재하는 이메일
            given(accountRepository.existsByEmailId(VALID_EMAIL)).willReturn(true);
            
            // When: 이메일 존재 여부 확인
            boolean result = accountService.existsByEmail(VALID_EMAIL);
            
            // Then: 결과가 true여야 함
            assertThat(result).isTrue();
            // Then: Repository 메서드가 정확히 한 번 호출되어야 함
            then(accountRepository).should(times(1)).existsByEmailId(VALID_EMAIL);
        }
        
        @Test
        @DisplayName("존재하지 않는 이메일이 주어졌을 때 false를 반환해야 한다")
        void existsByEmail_WithNonExistingEmail_ShouldReturnFalse() {
            // Given: DB에 존재하지 않는 이메일
            String nonExistingEmail = "nonexisting@example.com";
            given(accountRepository.existsByEmailId(nonExistingEmail)).willReturn(false);
            
            // When: 이메일 존재 여부 확인
            boolean result = accountService.existsByEmail(nonExistingEmail);
            
            // Then: 결과가 false여야 함
            assertThat(result).isFalse();
            // Then: Repository 메서드가 정확히 한 번 호출되어야 함
            then(accountRepository).should(times(1)).existsByEmailId(nonExistingEmail);
        }
        
        @Test
        @DisplayName("잘못된 이메일 형식이 주어졌을 때 AccountOperationException이 발생해야 한다")
        void existsByEmail_WithInvalidEmail_ShouldThrowAccountOperationException() {
            // Given: 잘못된 형식의 이메일
            String invalidEmail = "invalidemail";
            
            // When & Then: AccountOperationException이 발생해야 함
            assertThatThrownBy(() -> accountService.existsByEmail(invalidEmail))
                    .isInstanceOf(AccountOperationException.class)
                    .hasMessageContaining("이메일의 형식이 올바르지 않습니다");
            
            // Then: Repository 메서드는 호출되지 않아야 함
            then(accountRepository).should(never()).existsByEmailId(anyString());
        }
    }
    
    @Nested
    @DisplayName("signUp 메서드 테스트")
    class SignUpTest {
        
        @Test
        @DisplayName("유효한 회원가입 정보가 주어졌을 때 회원가입이 성공해야 한다")
        void signUp_WithValidInput_ShouldSucceed() {
            // Given: 유효한 회원가입 정보와 모든 외부 의존성이 성공하는 상황
            given(accountRepository.existsByEmailId(VALID_EMAIL)).willReturn(false);
            given(authCodeService.verifyAuthCode(any(EmailVerificationDto.class))).willReturn(true);
            given(passwordEncoder.encode(VALID_PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(accountRepository.save(any(AccountEntity.class))).willReturn(validAccountEntity);
            
            // When: 회원가입 실행
            assertThatCode(() -> accountService.signUp(validAccountDto, VALID_AUTH_CODE, SIGNUP_PURPOSE))
                    .doesNotThrowAnyException();
            
            // Then: 모든 외부 의존성이 정확히 호출되어야 함
            then(accountRepository).should(times(1)).existsByEmailId(VALID_EMAIL);
            then(authCodeService).should(times(1)).verifyAuthCode(any(EmailVerificationDto.class));
            then(passwordEncoder).should(times(1)).encode(VALID_PASSWORD);
            then(accountRepository).should(times(1)).save(any(AccountEntity.class));
        }
        
        @Test
        @DisplayName("이미 존재하는 이메일로 회원가입을 시도했을 때 EmailAlreadyExistsException이 발생해야 한다")
        void signUp_WithDuplicateEmail_ShouldThrowEmailAlreadyExistsException() {
            // Given: 이미 존재하는 이메일
            given(accountRepository.existsByEmailId(VALID_EMAIL)).willReturn(true);
            
            // When & Then: EmailAlreadyExistsException이 발생해야 함
            assertThatThrownBy(() -> accountService.signUp(validAccountDto, VALID_AUTH_CODE, SIGNUP_PURPOSE))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining("이미 존재하는 이메일입니다");
            
            // Then: 중복 검사 이후의 메서드들은 호출되지 않아야 함
            then(accountRepository).should(times(1)).existsByEmailId(VALID_EMAIL);
            then(authCodeService).should(never()).verifyAuthCode(any(EmailVerificationDto.class));
            then(passwordEncoder).should(never()).encode(anyString());
            then(accountRepository).should(never()).save(any(AccountEntity.class));
        }
        
        @Test
        @DisplayName("잘못된 인증 코드로 회원가입을 시도했을 때 InvalidAuthCodeException이 발생해야 한다")
        void signUp_WithInvalidAuthCode_ShouldThrowInvalidAuthCodeException() {
            // Given: 유효하지 않은 인증 코드
            given(accountRepository.existsByEmailId(VALID_EMAIL)).willReturn(false);
            given(authCodeService.verifyAuthCode(any(EmailVerificationDto.class))).willReturn(false);
            
            // When & Then: InvalidAuthCodeException이 발생해야 함
            assertThatThrownBy(() -> accountService.signUp(validAccountDto, VALID_AUTH_CODE, SIGNUP_PURPOSE))
                    .isInstanceOf(InvalidAuthCodeException.class)
                    .hasMessageContaining("인증 코드가 유효하지 않습니다");
            
            // Then: 인증 코드 검증까지만 실행되어야 함
            then(accountRepository).should(times(1)).existsByEmailId(VALID_EMAIL);
            then(authCodeService).should(times(1)).verifyAuthCode(any(EmailVerificationDto.class));
            then(passwordEncoder).should(never()).encode(anyString());
            then(accountRepository).should(never()).save(any(AccountEntity.class));
        }
        
        @Test
        @DisplayName("null AccountDto가 주어졌을 때 AccountOperationException이 발생해야 한다")
        void signUp_WithNullAccountDto_ShouldThrowAccountOperationException() {
            // Given: null AccountDto
            AccountDto nullAccountDto = null;
            
            // When & Then: AccountOperationException이 발생해야 함
            assertThatThrownBy(() -> accountService.signUp(nullAccountDto, VALID_AUTH_CODE, SIGNUP_PURPOSE))
                    .isInstanceOf(AccountOperationException.class)
                    .hasMessageContaining("계정 정보은(는) 필수 입력 항목입니다");
            
            // Then: 외부 의존성은 호출되지 않아야 함
            then(accountRepository).should(never()).existsByEmailId(anyString());
            then(authCodeService).should(never()).verifyAuthCode(any(EmailVerificationDto.class));
        }
        
        @Test
        @DisplayName("잘못된 형식의 인증 코드가 주어졌을 때 AccountOperationException이 발생해야 한다")
        void signUp_WithInvalidAuthCodeFormat_ShouldThrowAccountOperationException() {
            // Given: 잘못된 형식의 인증 코드 (5자리)
            String invalidAuthCode = "12345";
            
            // When & Then: AccountOperationException이 발생해야 함
            assertThatThrownBy(() -> accountService.signUp(validAccountDto, invalidAuthCode, SIGNUP_PURPOSE))
                    .isInstanceOf(AccountOperationException.class)
                    .hasMessageContaining("인증 코드은(는) 6자리 숫자여야 합니다");
            
            // Then: 외부 의존성은 호출되지 않아야 함
            then(accountRepository).should(never()).existsByEmailId(anyString());
            then(authCodeService).should(never()).verifyAuthCode(any(EmailVerificationDto.class));
        }
        
        @Test
        @DisplayName("짧은 비밀번호가 주어졌을 때 AccountOperationException이 발생해야 한다")
        void signUp_WithShortPassword_ShouldThrowAccountOperationException() {
            // Given: 짧은 비밀번호 (7자)
            AccountDto shortPasswordDto = AccountDto.builder()
                    .emailId(VALID_EMAIL)
                    .password("short")
                    .name("테스트사용자")
                    .build();
            
            // When & Then: AccountOperationException이 발생해야 함
            assertThatThrownBy(() -> accountService.signUp(shortPasswordDto, VALID_AUTH_CODE, SIGNUP_PURPOSE))
                    .isInstanceOf(AccountOperationException.class)
                    .hasMessageContaining("비밀번호은(는) 최소 8자 이상이어야 합니다");
            
            // Then: 외부 의존성은 호출되지 않아야 함
            then(accountRepository).should(never()).existsByEmailId(anyString());
            then(authCodeService).should(never()).verifyAuthCode(any(EmailVerificationDto.class));
        }
    }
    
    @Nested
    @DisplayName("getAccountEntityOrThrow 메서드 테스트")
    class GetAccountEntityOrThrowTest {
        
        @Test
        @DisplayName("존재하는 이메일 ID가 주어졌을 때 AccountEntity를 반환해야 한다")
        void getAccountEntityOrThrow_WithExistingEmail_ShouldReturnAccountEntity() {
            // Given: 존재하는 이메일 ID
            given(accountRepository.findByEmailId(VALID_EMAIL)).willReturn(Optional.of(validAccountEntity));
            
            // When: AccountEntity 조회
            AccountEntity result = accountService.getAccountEntityOrThrow(VALID_EMAIL);
            
            // Then: 올바른 AccountEntity가 반환되어야 함
            assertThat(result).isNotNull();
            assertThat(result.getEmailId()).isEqualTo(VALID_EMAIL);
            assertThat(result.getPassword()).isEqualTo(ENCODED_PASSWORD);
            
            // Then: Repository 메서드가 정확히 한 번 호출되어야 함
            then(accountRepository).should(times(1)).findByEmailId(VALID_EMAIL);
        }
        
        @Test
        @DisplayName("존재하지 않는 이메일 ID가 주어졌을 때 AccountNotFoundException이 발생해야 한다")
        void getAccountEntityOrThrow_WithNonExistentEmail_ShouldThrowAccountNotFoundException() {
            // Given: 존재하지 않는 이메일 ID
            String nonExistentEmail = "nonexistent@example.com";
            given(accountRepository.findByEmailId(nonExistentEmail)).willReturn(Optional.empty());
            
            // When & Then: AccountNotFoundException이 발생해야 함
            assertThatThrownBy(() -> accountService.getAccountEntityOrThrow(nonExistentEmail))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining("사용자가 존재하지 않습니다: " + nonExistentEmail);
            
            // Then: Repository 메서드가 정확히 한 번 호출되어야 함
            then(accountRepository).should(times(1)).findByEmailId(nonExistentEmail);
        }
    }
}
