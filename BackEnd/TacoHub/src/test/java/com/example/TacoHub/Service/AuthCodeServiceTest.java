package com.example.TacoHub.Service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.TacoHub.Dto.EmailVerificationDto;
import com.example.TacoHub.Exception.AuthCodeOperationException;

/**
 * AuthCodeService 단위 테스트
 * Given/When/Then 패턴과 현업 수준의 테스트 케이스 작성
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthCodeService 단위 테스트")
public class AuthCodeServiceTest {

    @Mock
    private RedisService<String> redisService;
    
    @InjectMocks
    private AuthCodeService authCodeService;
    
    // 테스트용 상수 정의
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PURPOSE = "회원가입";
    private static final String VALID_AUTH_CODE = "123456";
    private static final String REDIS_KEY = VALID_EMAIL + "_" + VALID_PURPOSE;
    
    private EmailVerificationDto validEmailVerificationDto;
    
    @BeforeEach
    void setUp() {
        // Given: 테스트용 유효한 EmailVerificationDto 준비
        validEmailVerificationDto = new EmailVerificationDto(VALID_EMAIL, VALID_AUTH_CODE, VALID_PURPOSE);
    }
    
    @Nested
    @DisplayName("createAuthCode 메서드 테스트")
    class CreateAuthCodeTest {
        
        @Test
        @DisplayName("인증 코드 생성이 성공하면 6자리 숫자 문자열을 반환해야 한다")
        void createAuthCode_ShouldReturnSixDigitString() {
            // When: 인증 코드 생성 실행
            String result = authCodeService.createAuthCode();
            
            // Then: 6자리 숫자 문자열이 반환되어야 함
            assertThat(result).isNotNull();
            assertThat(result).hasSize(6);
            assertThat(result).matches("\\d{6}");
        }
        
        @Test
        @DisplayName("여러 번 생성한 인증 코드는 서로 다른 값이어야 한다")
        void createAuthCode_ShouldGenerateDifferentCodes() {
            // When: 인증 코드를 여러 번 생성
            String code1 = authCodeService.createAuthCode();
            String code2 = authCodeService.createAuthCode();
            String code3 = authCodeService.createAuthCode();
            
            // Then: 생성된 코드들이 서로 달라야 함 (암호학적으로 안전한 랜덤)
            assertThat(code1).isNotEqualTo(code2);
            assertThat(code2).isNotEqualTo(code3);
            assertThat(code1).isNotEqualTo(code3);
        }
    }
    
    @Nested
    @DisplayName("verifyAuthCode 메서드 테스트")
    class VerifyAuthCodeTest {
        
        @Test
        @DisplayName("유효한 인증 코드가 주어졌을 때 검증이 성공해야 한다")
        void verifyAuthCode_WithValidCode_ShouldReturnTrue() {
            // Given: Redis에 저장된 유효한 인증 코드
            given(redisService.getValues(REDIS_KEY)).willReturn(VALID_AUTH_CODE);
            
            // When: 인증 코드 검증 실행
            boolean result = authCodeService.verifyAuthCode(validEmailVerificationDto);
            
            // Then: 검증 결과가 true여야 함
            assertThat(result).isTrue();
            
            // Then: Redis에서 인증 코드를 조회해야 함
            then(redisService).should(times(1)).getValues(REDIS_KEY);
        }
        
        @Test
        @DisplayName("잘못된 인증 코드가 주어졌을 때 검증이 실패해야 한다")
        void verifyAuthCode_WithInvalidCode_ShouldReturnFalse() {
            // Given: Redis에 저장된 다른 인증 코드
            String storedCode = "654321";
            given(redisService.getValues(REDIS_KEY)).willReturn(storedCode);
            
            // When: 잘못된 인증 코드로 검증 실행
            boolean result = authCodeService.verifyAuthCode(validEmailVerificationDto);
            
            // Then: 검증 결과가 false여야 함
            assertThat(result).isFalse();
            
            // Then: Redis에서 인증 코드를 조회해야 함
            then(redisService).should(times(1)).getValues(REDIS_KEY);
        }
        
        @Test
        @DisplayName("만료된 인증 코드가 주어졌을 때 검증이 실패해야 한다")
        void verifyAuthCode_WithExpiredCode_ShouldReturnFalse() {
            // Given: Redis에서 인증 코드가 존재하지 않음 (만료됨)
            given(redisService.getValues(REDIS_KEY)).willReturn(null);
            
            // When: 만료된 인증 코드로 검증 실행
            boolean result = authCodeService.verifyAuthCode(validEmailVerificationDto);
            
            // Then: 검증 결과가 false여야 함
            assertThat(result).isFalse();
            
            // Then: Redis에서 인증 코드를 조회해야 함
            then(redisService).should(times(1)).getValues(REDIS_KEY);
        }
        
        @Test
        @DisplayName("null EmailVerificationDto가 주어졌을 때 AuthCodeOperationException이 발생해야 한다")
        void verifyAuthCode_WithNullDto_ShouldThrowAuthCodeOperationException() {
            // Given: null EmailVerificationDto
            EmailVerificationDto nullDto = null;
            
            // When & Then: AuthCodeOperationException이 발생해야 함
            assertThatThrownBy(() -> authCodeService.verifyAuthCode(nullDto))
                    .isInstanceOf(AuthCodeOperationException.class)
                    .hasMessageContaining("인증 정보은(는) 필수 입력 항목입니다");
            
            // Then: Redis 메서드는 호출되지 않아야 함
            then(redisService).should(never()).getValues(anyString());
        }
        
        @Test
        @DisplayName("잘못된 인증 코드 형식이 주어졌을 때 AuthCodeOperationException이 발생해야 한다")
        void verifyAuthCode_WithInvalidCodeFormat_ShouldThrowAuthCodeOperationException() {
            // Given: 잘못된 형식의 인증 코드 (5자리)
            EmailVerificationDto invalidDto = new EmailVerificationDto(VALID_EMAIL, "12345", VALID_PURPOSE);
            
            // When & Then: AuthCodeOperationException이 발생해야 함
            assertThatThrownBy(() -> authCodeService.verifyAuthCode(invalidDto))
                    .isInstanceOf(AuthCodeOperationException.class)
                    .hasMessageContaining("인증 코드은(는) 6자리 숫자여야 합니다");
            
            // Then: Redis 메서드는 호출되지 않아야 함
            then(redisService).should(never()).getValues(anyString());
        }
        
        @Test
        @DisplayName("잘못된 이메일 형식이 주어졌을 때 AuthCodeOperationException이 발생해야 한다")
        void verifyAuthCode_WithInvalidEmail_ShouldThrowAuthCodeOperationException() {
            // Given: 잘못된 이메일 형식 (@ 없음)
            EmailVerificationDto invalidDto = new EmailVerificationDto("invalidemail", VALID_AUTH_CODE, VALID_PURPOSE);
            
            // When & Then: AuthCodeOperationException이 발생해야 함
            assertThatThrownBy(() -> authCodeService.verifyAuthCode(invalidDto))
                    .isInstanceOf(AuthCodeOperationException.class)
                    .hasMessageContaining("이메일의 형식이 올바르지 않습니다");
            
            // Then: Redis 메서드는 호출되지 않아야 함
            then(redisService).should(never()).getValues(anyString());
        }
        
        @Test
        @DisplayName("잘못된 인증 목적이 주어졌을 때 AuthCodeOperationException이 발생해야 한다")
        void verifyAuthCode_WithInvalidPurpose_ShouldThrowAuthCodeOperationException() {
            // Given: 잘못된 인증 목적
            EmailVerificationDto invalidDto = new EmailVerificationDto(VALID_EMAIL, VALID_AUTH_CODE, "잘못된목적");
            
            // When & Then: AuthCodeOperationException이 발생해야 함
            assertThatThrownBy(() -> authCodeService.verifyAuthCode(invalidDto))
                    .isInstanceOf(AuthCodeOperationException.class)
                    .hasMessageContaining("인증 목적이(가) 유효하지 않습니다");
            
            // Then: Redis 메서드는 호출되지 않아야 함
            then(redisService).should(never()).getValues(anyString());
        }
    }
    
    @Nested
    @DisplayName("setAuthCodeInRedis 메서드 테스트")
    class SetAuthCodeInRedisTest {
        
        @Test
        @DisplayName("유효한 정보가 주어졌을 때 Redis에 인증 코드가 저장되어야 한다")
        void setAuthCodeInRedis_WithValidInput_ShouldSucceed() {
            // Given: 유효한 입력값
            String email = VALID_EMAIL;
            String authCode = VALID_AUTH_CODE;
            String purpose = VALID_PURPOSE;
            
            // When: Redis에 인증 코드 저장 실행
            assertThatCode(() -> authCodeService.setAuthCodeInRedis(email, authCode, purpose))
                    .doesNotThrowAnyException();
            
            // Then: Redis setValues 메서드가 호출되어야 함
            then(redisService).should(times(1)).setValues(eq(REDIS_KEY), eq(authCode), any(Duration.class));
        }
        
        @Test
        @DisplayName("null 이메일이 주어졌을 때 AuthCodeOperationException이 발생해야 한다")
        void setAuthCodeInRedis_WithNullEmail_ShouldThrowAuthCodeOperationException() {
            // Given: null 이메일
            String nullEmail = null;
            
            // When & Then: AuthCodeOperationException이 발생해야 함
            assertThatThrownBy(() -> authCodeService.setAuthCodeInRedis(nullEmail, VALID_AUTH_CODE, VALID_PURPOSE))
                    .isInstanceOf(AuthCodeOperationException.class)
                    .hasMessageContaining("이메일은(는) 필수 입력 항목입니다");
            
            // Then: Redis 메서드는 호출되지 않아야 함
            then(redisService).should(never()).setValues(anyString(), anyString(), any(Duration.class));
        }
        
        @Test
        @DisplayName("잘못된 인증 코드가 주어졌을 때 AuthCodeOperationException이 발생해야 한다")
        void setAuthCodeInRedis_WithInvalidAuthCode_ShouldThrowAuthCodeOperationException() {
            // Given: 잘못된 인증 코드 (문자 포함)
            String invalidAuthCode = "12a456";
            
            // When & Then: AuthCodeOperationException이 발생해야 함
            assertThatThrownBy(() -> authCodeService.setAuthCodeInRedis(VALID_EMAIL, invalidAuthCode, VALID_PURPOSE))
                    .isInstanceOf(AuthCodeOperationException.class)
                    .hasMessageContaining("인증 코드은(는) 숫자만 입력 가능합니다");
            
            // Then: Redis 메서드는 호출되지 않아야 함
            then(redisService).should(never()).setValues(anyString(), anyString(), any(Duration.class));
        }
    }
    
    @Nested
    @DisplayName("deleteAuthCodeInRedis 메서드 테스트")
    class DeleteAuthCodeInRedisTest {
        
        @Test
        @DisplayName("유효한 정보가 주어졌을 때 Redis에서 인증 코드가 삭제되어야 한다")
        void deleteAuthCodeInRedis_WithValidInput_ShouldSucceed() {
            // Given: 유효한 입력값
            String email = VALID_EMAIL;
            String purpose = VALID_PURPOSE;
            
            // When: Redis에서 인증 코드 삭제 실행
            assertThatCode(() -> authCodeService.deleteAuthCodeInRedis(email, purpose))
                    .doesNotThrowAnyException();
            
            // Then: Redis deleteValues 메서드가 호출되어야 함
            then(redisService).should(times(1)).deleteValues(REDIS_KEY);
        }
        
        @Test
        @DisplayName("null 이메일이 주어졌을 때 AuthCodeOperationException이 발생해야 한다")
        void deleteAuthCodeInRedis_WithNullEmail_ShouldThrowAuthCodeOperationException() {
            // Given: null 이메일
            String nullEmail = null;
            
            // When & Then: AuthCodeOperationException이 발생해야 함
            assertThatThrownBy(() -> authCodeService.deleteAuthCodeInRedis(nullEmail, VALID_PURPOSE))
                    .isInstanceOf(AuthCodeOperationException.class)
                    .hasMessageContaining("이메일은(는) 필수 입력 항목입니다");
            
            // Then: Redis 메서드는 호출되지 않아야 함
            then(redisService).should(never()).deleteValues(anyString());
        }
        
        @Test
        @DisplayName("null 목적이 주어졌을 때 AuthCodeOperationException이 발생해야 한다")
        void deleteAuthCodeInRedis_WithNullPurpose_ShouldThrowAuthCodeOperationException() {
            // Given: null 목적
            String nullPurpose = null;
            
            // When & Then: AuthCodeOperationException이 발생해야 함
            assertThatThrownBy(() -> authCodeService.deleteAuthCodeInRedis(VALID_EMAIL, nullPurpose))
                    .isInstanceOf(AuthCodeOperationException.class)
                    .hasMessageContaining("인증 목적은(는) 필수 입력 항목입니다");
            
            // Then: Redis 메서드는 호출되지 않아야 함
            then(redisService).should(never()).deleteValues(anyString());
        }
    }
}

