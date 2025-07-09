package com.example.TacoHub.Service;

import com.example.TacoHub.Exception.EmailOperationException;
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
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final AuthCodeService authCodeService;

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
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            return message;
        } catch (Exception e) {
            handleAndThrowEmailException("createEmailMessage", e);
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
    @Transactional
    public void sendAuthCodeToEmail(String to, String purpose) {
        try {
            // 입력값 검증
            if (to == null || to.trim().isEmpty()) {
                log.warn("이메일 주소가 비어있음");
                throw new EmailOperationException("이메일 주소는 필수입니다");
            }
            if (purpose == null || purpose.trim().isEmpty()) {
                log.warn("인증 목적이 비어있음");
                throw new EmailOperationException("인증 목적은 필수입니다");
            }

            String title = "TacoHub 회원가입 인증 코드";
            String authCode = authCodeService.createAuthCode();
            String text = "인증 코드: " + authCode;

            authCodeService.setAuthCodeInRedis(to, authCode, purpose);

            SimpleMailMessage message = createEmailMessage(to, title, text);
            javaMailSender.send(message);

            log.info("이메일 인증 코드 발송 완료: {}, 목적: {}", to, purpose);
        } catch (EmailOperationException e) {
            log.warn("이메일 발송 비즈니스 오류: {}", e.getMessage());
            throw e; // 비즈니스 예외는 그대로 전파
        } catch (Exception e) {
            handleAndThrowEmailException("sendAuthCodeToEmail", e);
        }
    }

    /**
     * 공통 Email 예외 처리 메서드
     * @param methodName 실패한 메서드명
     * @param originalException 원본 예외
     * @throws EmailOperationException 래핑된 예외
     */
    private void handleAndThrowEmailException(String methodName, Exception originalException) {
        String errorMessage = originalException.getMessage();
        String exceptionType = originalException.getClass().getSimpleName();
        
        log.error("{} 실패: type={}, message={}", methodName, exceptionType, errorMessage, originalException);
        
        throw new EmailOperationException(
            String.format("%s 실패 [%s]: %s", methodName, exceptionType, errorMessage),
            originalException
        );
    }


}
