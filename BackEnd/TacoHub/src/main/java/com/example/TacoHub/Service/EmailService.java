package com.example.TacoHub.Service;

import com.example.TacoHub.Exception.EmailOperationException;
import com.example.TacoHub.Exception.BusinessException;
import com.example.TacoHub.Logging.AuditLogging;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이메일 전송 기능을 제공하는 서비스 클래스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService extends BaseService {

    private final JavaMailSender javaMailSender;
    private final AuthCodeService authCodeService;

    private final String invitationEmailUrl = ""; // TODO : Value 써서 yml 및 env 설정 관리 필요

    // ========== 공통 검증 메서드 ==========
    
    /**
     * 이메일 주소 유효성 검증
     * @param email 검증할 이메일 주소
     * @param paramName 매개변수명 (로그용)
     */
    private void validateEmail(String email, String paramName) {
        if (isStringNullOrEmpty(email)) {
            throw new EmailOperationException(paramName + "은(는) 필수 입력 항목입니다. 이메일 주소를 입력해주세요.");
        }
        if (isStringTooLong(email, 255)) {
            throw new EmailOperationException(paramName + "은(는) 255자를 초과할 수 없습니다. 현재 길이: " + email.length() + "자");
        }
        // 기본적인 이메일 형식 검증
        if (!email.contains("@")) {
            throw new EmailOperationException(paramName + "의 형식이 올바르지 않습니다. 올바른 이메일 주소를 입력해주세요.");
        }
    }

    /**
     * 인증 목적 유효성 검증
     * @param purpose 검증할 인증 목적
     * @param paramName 매개변수명 (로그용)
     */
    private void validatePurpose(String purpose, String paramName) {
        if (isStringNullOrEmpty(purpose)) {
            throw new EmailOperationException(paramName + "은(는) 필수 입력 항목입니다. 인증 목적을 지정해주세요.");
        }
        // 허용된 인증 목적인지 검증
        if (!purpose.equals("회원가입") && !purpose.equals("비밀번호재설정") && !purpose.equals("이메일변경")) {
            throw new EmailOperationException(paramName + "이(가) 유효하지 않습니다. 허용된 인증 목적: 회원가입, 비밀번호재설정, 이메일변경");
        }
    }

    /**
     * 이메일 제목 유효성 검증
     * @param subject 검증할 이메일 제목
     * @param paramName 매개변수명 (로그용)
     */
    private void validateSubject(String subject, String paramName) {
        if (isStringNullOrEmpty(subject)) {
            throw new EmailOperationException(paramName + "은(는) 필수 입력 항목입니다. 이메일 제목을 입력해주세요.");
        }
        if (isStringTooLong(subject, 500)) {
            throw new EmailOperationException(paramName + "은(는) 500자를 초과할 수 없습니다. 현재 길이: " + subject.length() + "자");
        }
    }

    /**
     * 이메일 내용 유효성 검증
     * @param text 검증할 이메일 내용
     * @param paramName 매개변수명 (로그용)
     */
    private void validateText(String text, String paramName) {
        if (isStringNullOrEmpty(text)) {
            throw new EmailOperationException(paramName + "은(는) 필수 입력 항목입니다. 이메일 내용을 입력해주세요.");
        }
        if (isStringTooLong(text, 10000)) {
            throw new EmailOperationException(paramName + "은(는) 10000자를 초과할 수 없습니다. 현재 길이: " + text.length() + "자");
        }
    }

    /**
     * 이메일 메시지 객체를 생성한다.
     * 
     * @param to 수신자 이메일 주소
     * @param subject 이메일 제목
     * @param text 이메일 내용
     * @return 생성된 SimpleMailMessage 객체
     * @throws EmailOperationException 이메일 메시지 생성 중 오류 발생 시
     */
    private SimpleMailMessage createEmailMessage(String to, String subject, String text) {
        String methodName = "createEmailMessage";
        
        try {
            // 1. 입력값 검증
            validateEmail(to, "수신자 이메일");
            validateSubject(subject, "이메일 제목");
            validateText(text, "이메일 내용");

            // 2. 이메일 메시지 생성
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            return message;
            
        } catch (EmailOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowEmailException(methodName, e);
            return null; // 실제로는 도달하지 않음
        }
    }

    /**
     * 인증 코드를 이메일로 전송한다.
     * 
     * @param to 수신자 이메일 주소
     * @param purpose 인증 목적(회원가입, 비밀번호 재설정 등)
     * @throws EmailOperationException 이메일 전송 중 오류 발생 시
     */
    @AuditLogging(action = "인증코드_이메일_전송", includeParameters = true, includePerformance = true)
    @Transactional
    public void sendAuthCodeToEmail(String to, String purpose) {
        String methodName = "sendAuthCodeToEmail";
        log.info("[{}] 인증 코드 이메일 전송 시작: to={}, purpose={}", methodName, to, purpose);
        
        try {
            // 1. 입력값 검증
            validateEmail(to, "수신자 이메일");
            validatePurpose(purpose, "인증 목적");

            // 2. 이메일 내용 생성
            String title = "TacoHub 회원가입 인증 코드";
            String authCode = authCodeService.createAuthCode();
            String text = "인증 코드: " + authCode;

            // 3. Redis에 인증 코드 저장
            authCodeService.setAuthCodeInRedis(to, authCode, purpose);

            // 4. 이메일 전송
            SimpleMailMessage message = createEmailMessage(to, title, text);
            javaMailSender.send(message);

            log.info("[{}] 인증 코드 이메일 전송 완료: to={}, purpose={}", methodName, to, purpose);
            
        } catch (EmailOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowEmailException(methodName, e);
        }
    }

    /**
     * 워크스페이스 초대 이메일을 전송한다.
     * 
     * @param to 수신자 이메일 주소
     * @param invitationToken 초대 토큰
     * @param workspaceName 워크스페이스 이름
     * @param inviterName 초대한 사람 이름
     * @param role 부여될 역할
     * @param customMessage 사용자 정의 메시지 (선택사항)
     * @return 이메일 전송 성공 여부
     * @throws EmailOperationException 이메일 전송 중 오류 발생 시
     */
    @AuditLogging(action = "초대_이메일_전송", includeParameters = true, includePerformance = true)
    public boolean sendInvitationEmail(String to, String invitationToken, String workspaceName, 
                                     String inviterName, String role, String customMessage) {
        String methodName = "sendInvitationEmail";
        log.info("[{}] 초대 이메일 전송 시작: to={}, workspaceName={}, role={}", methodName, to, workspaceName, role);
        
        try {
            // 1. 입력값 검증
            validateEmail(to, "수신자 이메일");
            validateInvitationToken(invitationToken, "초대 토큰");
            validateWorkspaceName(workspaceName, "워크스페이스 이름");
            validateInviterName(inviterName, "초대자 이름");
            validateRole(role, "역할");

            // 2. 이메일 내용 생성
            String subject = String.format("[TacoHub] %s님이 '%s' 워크스페이스에 초대하셨습니다", inviterName, workspaceName);
            String emailText = createInvitationEmailText(to, invitationToken, workspaceName, inviterName, role, customMessage);

            // 3. 이메일 전송
            SimpleMailMessage message = createEmailMessage(to, subject, emailText);
            javaMailSender.send(message);

            log.info("[{}] 초대 이메일 전송 완료: to={}, workspaceName={}, role={}", methodName, to, workspaceName, role);
            return true;
            
        } catch (EmailOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[{}] 초대 이메일 전송 실패: to={}, error={}", methodName, to, e.getMessage());
            return false; // 이메일 전송 실패는 시스템을 중단시키지 않음
        }
    }

    /**
     * 초대 이메일 텍스트 생성
     */
    private String createInvitationEmailText(String to, String invitationToken, String workspaceName, 
                                           String inviterName, String role, String customMessage) {
        StringBuilder emailText = new StringBuilder();
        
        emailText.append("안녕하세요!\n\n");
        emailText.append(String.format("%s님이 TacoHub의 '%s' 워크스페이스에 초대하셨습니다.\n\n", inviterName, workspaceName));
        emailText.append(String.format("부여될 역할: %s\n\n", role));
        
        // 사용자 정의 메시지가 있는 경우 추가
        if (customMessage != null && !customMessage.trim().isEmpty()) {
            emailText.append("초대 메시지:\n");
            emailText.append(customMessage);
            emailText.append("\n\n");
        }
        
        emailText.append("초대를 수락하려면 아래 링크를 클릭하세요:\n");
        // TODO: 실제 프론트엔드 URL로 변경 필요
        emailText.append(String.format("https://tacohub.com/accept-invitation?token=%s\n\n", invitationToken));
        
        emailText.append("이 초대는 7일간 유효합니다.\n\n");
        emailText.append("감사합니다.\n");
        emailText.append("TacoHub 팀");
        
        return emailText.toString();
    }

    /**
     * 초대 토큰 유효성 검증
     */
    private void validateInvitationToken(String token, String paramName) {
        if (isStringNullOrEmpty(token)) {
            throw new EmailOperationException(paramName + "은(는) 필수 입력 항목입니다.");
        }
    }

    /**
     * 워크스페이스 이름 유효성 검증
     */
    private void validateWorkspaceName(String workspaceName, String paramName) {
        if (isStringNullOrEmpty(workspaceName)) {
            throw new EmailOperationException(paramName + "은(는) 필수 입력 항목입니다.");
        }
        if (isStringTooLong(workspaceName, 100)) {
            throw new EmailOperationException(paramName + "은(는) 100자를 초과할 수 없습니다.");
        }
    }

    /**
     * 초대자 이름 유효성 검증
     */
    private void validateInviterName(String inviterName, String paramName) {
        if (isStringNullOrEmpty(inviterName)) {
            throw new EmailOperationException(paramName + "은(는) 필수 입력 항목입니다.");
        }
        if (isStringTooLong(inviterName, 100)) {
            throw new EmailOperationException(paramName + "은(는) 100자를 초과할 수 없습니다.");
        }
    }

    /**
     * 역할 유효성 검증
     */
    private void validateRole(String role, String paramName) {
        if (isStringNullOrEmpty(role)) {
            throw new EmailOperationException(paramName + "은(는) 필수 입력 항목입니다.");
        }
        if (!role.equals("ADMIN") && !role.equals("MEMBER") && !role.equals("GUEST")) {
            throw new EmailOperationException(paramName + "은(는) ADMIN, MEMBER, GUEST 중 하나여야 합니다.");
        }
    }

    /**
     * 공통 Email 예외 처리 메서드
     * 예외 타입에 따라 자동으로 warn/error 로깅을 결정
     * 
     * @param methodName 실패한 메서드명
     * @param originalException 원본 예외
     * @throws EmailOperationException 래핑된 예외
     */
    private void handleAndThrowEmailException(String methodName, Exception originalException) {
        EmailOperationException customException = new EmailOperationException(
            String.format("%s 실패 [%s]: %s", methodName, 
                         originalException.getClass().getSimpleName(), 
                         originalException.getMessage()),
            originalException
        );
        
        // BaseService의 메서드를 사용하여 예외 타입에 따라 warn/error 로깅
        handleAndThrow(methodName, originalException, customException);
    }

}
