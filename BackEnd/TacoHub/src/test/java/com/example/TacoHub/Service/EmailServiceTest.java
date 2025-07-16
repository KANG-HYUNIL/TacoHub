package com.example.TacoHub.Service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.example.TacoHub.Exception.EmailOperationException;

/**
 * EmailService 단위 테스트
 * Given/When/Then 패턴과 현업 수준의 테스트 케이스 작성
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService 단위 테스트")
public class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;
    
    @Mock
    private AuthCodeService authCodeService;
    
    @InjectMocks
    private EmailService emailService;
    
    // 테스트용 상수 정의
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PURPOSE = "회원가입";
    private static final String VALID_AUTH_CODE = "123456";
    private static final String VALID_INVITATION_TOKEN = "550e8400-e29b-41d4-a716-446655440000";
    private static final String VALID_WORKSPACE_NAME = "테스트 워크스페이스";
    private static final String VALID_INVITER_NAME = "초대자";
    private static final String VALID_ROLE = "MEMBER";
    private static final String CUSTOM_MESSAGE = "함께 작업해요!";
    
    @Nested
    @DisplayName("sendAuthCodeToEmail 메서드 테스트")
    class SendAuthCodeToEmailTest {
        
        @Test
        @DisplayName("유효한 이메일과 목적이 주어졌을 때 인증 코드 이메일 전송이 성공해야 한다")
        void sendAuthCodeToEmail_WithValidInput_ShouldSucceed() {
            // Given: 유효한 입력값과 AuthCodeService가 성공적으로 동작하는 상황
            given(authCodeService.createAuthCode()).willReturn(VALID_AUTH_CODE);
            doNothing().when(authCodeService).setAuthCodeInRedis(VALID_EMAIL, VALID_AUTH_CODE, VALID_PURPOSE);
            doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));
            
            // When: 인증 코드 이메일 전송 실행
            assertThatCode(() -> emailService.sendAuthCodeToEmail(VALID_EMAIL, VALID_PURPOSE))
                    .doesNotThrowAnyException();
            
            // Then: 모든 외부 의존성이 정확히 호출되어야 함
            then(authCodeService).should(times(1)).createAuthCode();
            then(authCodeService).should(times(1)).setAuthCodeInRedis(VALID_EMAIL, VALID_AUTH_CODE, VALID_PURPOSE);
            then(javaMailSender).should(times(1)).send(any(SimpleMailMessage.class));
        }
        
        @Test
        @DisplayName("null 이메일이 주어졌을 때 EmailOperationException이 발생해야 한다")
        void sendAuthCodeToEmail_WithNullEmail_ShouldThrowEmailOperationException() {
            // Given: null 이메일
            String nullEmail = null;
            
            // When & Then: EmailOperationException이 발생해야 함
            assertThatThrownBy(() -> emailService.sendAuthCodeToEmail(nullEmail, VALID_PURPOSE))
                    .isInstanceOf(EmailOperationException.class)
                    .hasMessageContaining("수신자 이메일은(는) 필수 입력 항목입니다");
            
            // Then: 외부 의존성은 호출되지 않아야 함
            then(authCodeService).should(never()).createAuthCode();
            then(authCodeService).should(never()).setAuthCodeInRedis(anyString(), anyString(), anyString());
            then(javaMailSender).should(never()).send(any(SimpleMailMessage.class));
        }
        
        @Test
        @DisplayName("빈 문자열 이메일이 주어졌을 때 EmailOperationException이 발생해야 한다")
        void sendAuthCodeToEmail_WithEmptyEmail_ShouldThrowEmailOperationException() {
            // Given: 빈 문자열 이메일
            String emptyEmail = "";
            
            // When & Then: EmailOperationException이 발생해야 함
            assertThatThrownBy(() -> emailService.sendAuthCodeToEmail(emptyEmail, VALID_PURPOSE))
                    .isInstanceOf(EmailOperationException.class)
                    .hasMessageContaining("수신자 이메일은(는) 필수 입력 항목입니다");
            
            // Then: 외부 의존성은 호출되지 않아야 함
            then(authCodeService).should(never()).createAuthCode();
            then(authCodeService).should(never()).setAuthCodeInRedis(anyString(), anyString(), anyString());
            then(javaMailSender).should(never()).send(any(SimpleMailMessage.class));
        }
        
        @Test
        @DisplayName("잘못된 이메일 형식이 주어졌을 때 EmailOperationException이 발생해야 한다")
        void sendAuthCodeToEmail_WithInvalidEmailFormat_ShouldThrowEmailOperationException() {
            // Given: 잘못된 이메일 형식 (@ 없음)
            String invalidEmail = "invalidemail";
            
            // When & Then: EmailOperationException이 발생해야 함
            assertThatThrownBy(() -> emailService.sendAuthCodeToEmail(invalidEmail, VALID_PURPOSE))
                    .isInstanceOf(EmailOperationException.class)
                    .hasMessageContaining("수신자 이메일의 형식이 올바르지 않습니다");
            
            // Then: 외부 의존성은 호출되지 않아야 함
            then(authCodeService).should(never()).createAuthCode();
            then(authCodeService).should(never()).setAuthCodeInRedis(anyString(), anyString(), anyString());
            then(javaMailSender).should(never()).send(any(SimpleMailMessage.class));
        }
        
        @Test
        @DisplayName("255자를 초과하는 이메일이 주어졌을 때 EmailOperationException이 발생해야 한다")
        void sendAuthCodeToEmail_WithTooLongEmail_ShouldThrowEmailOperationException() {
            // Given: 255자를 초과하는 이메일
            String tooLongEmail = "a".repeat(250) + "@example.com"; // 263자
            
            // When & Then: EmailOperationException이 발생해야 함
            assertThatThrownBy(() -> emailService.sendAuthCodeToEmail(tooLongEmail, VALID_PURPOSE))
                    .isInstanceOf(EmailOperationException.class)
                    .hasMessageContaining("수신자 이메일은(는) 255자를 초과할 수 없습니다");
            
            // Then: 외부 의존성은 호출되지 않아야 함
            then(authCodeService).should(never()).createAuthCode();
            then(authCodeService).should(never()).setAuthCodeInRedis(anyString(), anyString(), anyString());
            then(javaMailSender).should(never()).send(any(SimpleMailMessage.class));
        }
        
        @Test
        @DisplayName("null 목적이 주어졌을 때 EmailOperationException이 발생해야 한다")
        void sendAuthCodeToEmail_WithNullPurpose_ShouldThrowEmailOperationException() {
            // Given: null 목적
            String nullPurpose = null;
            
            // When & Then: EmailOperationException이 발생해야 함
            assertThatThrownBy(() -> emailService.sendAuthCodeToEmail(VALID_EMAIL, nullPurpose))
                    .isInstanceOf(EmailOperationException.class)
                    .hasMessageContaining("인증 목적은(는) 필수 입력 항목입니다");
            
            // Then: 외부 의존성은 호출되지 않아야 함
            then(authCodeService).should(never()).createAuthCode();
            then(authCodeService).should(never()).setAuthCodeInRedis(anyString(), anyString(), anyString());
            then(javaMailSender).should(never()).send(any(SimpleMailMessage.class));
        }
        
        @Test
        @DisplayName("허용되지 않은 인증 목적이 주어졌을 때 EmailOperationException이 발생해야 한다")
        void sendAuthCodeToEmail_WithInvalidPurpose_ShouldThrowEmailOperationException() {
            // Given: 허용되지 않은 인증 목적
            String invalidPurpose = "잘못된목적";
            
            // When & Then: EmailOperationException이 발생해야 함
            assertThatThrownBy(() -> emailService.sendAuthCodeToEmail(VALID_EMAIL, invalidPurpose))
                    .isInstanceOf(EmailOperationException.class)
                    .hasMessageContaining("인증 목적이(가) 유효하지 않습니다");
            
            // Then: 외부 의존성은 호출되지 않아야 함
            then(authCodeService).should(never()).createAuthCode();
            then(authCodeService).should(never()).setAuthCodeInRedis(anyString(), anyString(), anyString());
            then(javaMailSender).should(never()).send(any(SimpleMailMessage.class));
        }
        
        @Test
        @DisplayName("AuthCodeService에서 인증 코드 생성 실패 시 예외가 전파되어야 한다")
        void sendAuthCodeToEmail_WithAuthCodeCreationFailure_ShouldPropagateException() {
            // Given: AuthCodeService에서 예외 발생
            given(authCodeService.createAuthCode()).willThrow(new RuntimeException("인증 코드 생성 실패"));
            
            // When & Then: 예외가 전파되어야 함
            assertThatThrownBy(() -> emailService.sendAuthCodeToEmail(VALID_EMAIL, VALID_PURPOSE))
                    .isInstanceOf(EmailOperationException.class);
            
            // Then: createAuthCode만 호출되고 나머지는 호출되지 않아야 함
            then(authCodeService).should(times(1)).createAuthCode();
            then(authCodeService).should(never()).setAuthCodeInRedis(anyString(), anyString(), anyString());
            then(javaMailSender).should(never()).send(any(SimpleMailMessage.class));
        }
        
        @Test
        @DisplayName("JavaMailSender에서 이메일 전송 실패 시 예외가 전파되어야 한다")
        void sendAuthCodeToEmail_WithEmailSendFailure_ShouldPropagateException() {
            // Given: 이메일 전송에서 예외 발생
            given(authCodeService.createAuthCode()).willReturn(VALID_AUTH_CODE);
            doNothing().when(authCodeService).setAuthCodeInRedis(VALID_EMAIL, VALID_AUTH_CODE, VALID_PURPOSE);
            doThrow(new RuntimeException("이메일 전송 실패")).when(javaMailSender).send(any(SimpleMailMessage.class));
            
            // When & Then: 예외가 전파되어야 함
            assertThatThrownBy(() -> emailService.sendAuthCodeToEmail(VALID_EMAIL, VALID_PURPOSE))
                    .isInstanceOf(EmailOperationException.class);
            
            // Then: 이메일 전송까지 모든 메서드가 호출되어야 함
            then(authCodeService).should(times(1)).createAuthCode();
            then(authCodeService).should(times(1)).setAuthCodeInRedis(VALID_EMAIL, VALID_AUTH_CODE, VALID_PURPOSE);
            then(javaMailSender).should(times(1)).send(any(SimpleMailMessage.class));
        }
    }
    
    @Nested
    @DisplayName("sendInvitationEmail 메서드 테스트")
    class SendInvitationEmailTest {
        
        @Test
        @DisplayName("유효한 초대 정보가 주어졌을 때 초대 이메일 전송이 성공해야 한다")
        void sendInvitationEmail_WithValidInput_ShouldReturnTrue() {
            // Given: 유효한 초대 정보와 이메일 전송이 성공하는 상황
            doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));
            
            // When: 초대 이메일 전송 실행
            boolean result = emailService.sendInvitationEmail(
                    VALID_EMAIL, VALID_INVITATION_TOKEN, VALID_WORKSPACE_NAME, 
                    VALID_INVITER_NAME, VALID_ROLE, CUSTOM_MESSAGE);
            
            // Then: 전송 성공 여부가 true여야 함
            assertThat(result).isTrue();
            
            // Then: JavaMailSender가 호출되어야 함
            then(javaMailSender).should(times(1)).send(any(SimpleMailMessage.class));
        }
        
        @Test
        @DisplayName("커스텀 메시지가 null일 때도 초대 이메일 전송이 성공해야 한다")
        void sendInvitationEmail_WithNullCustomMessage_ShouldReturnTrue() {
            // Given: 커스텀 메시지가 null인 상황
            doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));
            
            // When: 초대 이메일 전송 실행 (커스텀 메시지 null)
            boolean result = emailService.sendInvitationEmail(
                    VALID_EMAIL, VALID_INVITATION_TOKEN, VALID_WORKSPACE_NAME, 
                    VALID_INVITER_NAME, VALID_ROLE, null);
            
            // Then: 전송 성공 여부가 true여야 함
            assertThat(result).isTrue();
            
            // Then: JavaMailSender가 호출되어야 함
            then(javaMailSender).should(times(1)).send(any(SimpleMailMessage.class));
        }
        
        @Test
        @DisplayName("null 이메일이 주어졌을 때 EmailOperationException이 발생해야 한다")
        void sendInvitationEmail_WithNullEmail_ShouldThrowEmailOperationException() {
            // Given: null 이메일
            String nullEmail = null;
            
            // When & Then: EmailOperationException이 발생해야 함
            assertThatThrownBy(() -> emailService.sendInvitationEmail(
                    nullEmail, VALID_INVITATION_TOKEN, VALID_WORKSPACE_NAME, 
                    VALID_INVITER_NAME, VALID_ROLE, CUSTOM_MESSAGE))
                    .isInstanceOf(EmailOperationException.class)
                    .hasMessageContaining("수신자 이메일은(는) 필수 입력 항목입니다");
            
            // Then: JavaMailSender는 호출되지 않아야 함
            then(javaMailSender).should(never()).send(any(SimpleMailMessage.class));
        }
        
        @Test
        @DisplayName("null 초대 토큰이 주어졌을 때 EmailOperationException이 발생해야 한다")
        void sendInvitationEmail_WithNullToken_ShouldThrowEmailOperationException() {
            // Given: null 초대 토큰
            String nullToken = null;
            
            // When & Then: EmailOperationException이 발생해야 함
            assertThatThrownBy(() -> emailService.sendInvitationEmail(
                    VALID_EMAIL, nullToken, VALID_WORKSPACE_NAME, 
                    VALID_INVITER_NAME, VALID_ROLE, CUSTOM_MESSAGE))
                    .isInstanceOf(EmailOperationException.class)
                    .hasMessageContaining("초대 토큰은(는) 필수 입력 항목입니다");
            
            // Then: JavaMailSender는 호출되지 않아야 함
            then(javaMailSender).should(never()).send(any(SimpleMailMessage.class));
        }
        
        @Test
        @DisplayName("null 워크스페이스 이름이 주어졌을 때 EmailOperationException이 발생해야 한다")
        void sendInvitationEmail_WithNullWorkspaceName_ShouldThrowEmailOperationException() {
            // Given: null 워크스페이스 이름
            String nullWorkspaceName = null;
            
            // When & Then: EmailOperationException이 발생해야 함
            assertThatThrownBy(() -> emailService.sendInvitationEmail(
                    VALID_EMAIL, VALID_INVITATION_TOKEN, nullWorkspaceName, 
                    VALID_INVITER_NAME, VALID_ROLE, CUSTOM_MESSAGE))
                    .isInstanceOf(EmailOperationException.class)
                    .hasMessageContaining("워크스페이스 이름은(는) 필수 입력 항목입니다");
            
            // Then: JavaMailSender는 호출되지 않아야 함
            then(javaMailSender).should(never()).send(any(SimpleMailMessage.class));
        }
        
        @Test
        @DisplayName("잘못된 역할이 주어졌을 때 EmailOperationException이 발생해야 한다")
        void sendInvitationEmail_WithInvalidRole_ShouldThrowEmailOperationException() {
            // Given: 잘못된 역할
            String invalidRole = "INVALID_ROLE";
            
            // When & Then: EmailOperationException이 발생해야 함
            assertThatThrownBy(() -> emailService.sendInvitationEmail(
                    VALID_EMAIL, VALID_INVITATION_TOKEN, VALID_WORKSPACE_NAME, 
                    VALID_INVITER_NAME, invalidRole, CUSTOM_MESSAGE))
                    .isInstanceOf(EmailOperationException.class)
                    .hasMessageContaining("역할이(가) 유효하지 않습니다");
            
            // Then: JavaMailSender는 호출되지 않아야 함
            then(javaMailSender).should(never()).send(any(SimpleMailMessage.class));
        }
        
        @Test
        @DisplayName("JavaMailSender에서 예외 발생 시 EmailOperationException이 발생해야 한다")
        void sendInvitationEmail_WithMailSenderException_ShouldThrowEmailOperationException() {
            // Given: JavaMailSender에서 예외 발생
            doThrow(new RuntimeException("이메일 전송 실패")).when(javaMailSender).send(any(SimpleMailMessage.class));
            
            // When & Then: EmailOperationException이 발생해야 함
            assertThatThrownBy(() -> emailService.sendInvitationEmail(
                    VALID_EMAIL, VALID_INVITATION_TOKEN, VALID_WORKSPACE_NAME, 
                    VALID_INVITER_NAME, VALID_ROLE, CUSTOM_MESSAGE))
                    .isInstanceOf(EmailOperationException.class);
            
            // Then: JavaMailSender가 호출되어야 함
            then(javaMailSender).should(times(1)).send(any(SimpleMailMessage.class));
        }
    }
}
