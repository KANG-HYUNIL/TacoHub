package com.example.TacoHub.Service;

import com.example.TacoHub.Exception.TechnicalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
     * @throws TechnicalException 이메일 메시지 생성 중 오류 발생 시
     */
    private SimpleMailMessage createEmailMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            return message;
        } catch (Exception e) {
            log.error("이메일 메시지 생성 중 오류 발생", e);
            throw new TechnicalException("이메일 메시지 생성 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 인증 코드를 이메일로 전송한다.
     * 
     * @param to 수신자 이메일 주소
     * @param purpose 인증 목적(회원가입, 비밀번호 재설정 등)
     * @throws TechnicalException 이메일 전송 중 오류 발생 시
     */
    @Transactional
    public void sendAuthCodeToEmail(String to, String purpose) {
        try {
            String title = "TacoHub 회원가입 인증 코드";
            String authCode = authCodeService.createAuthCode();
            String text = "인증 코드: " + authCode;

            authCodeService.setAuthCodeInRedis(to, authCode, purpose);

            SimpleMailMessage message = createEmailMessage(to, title, text);
            javaMailSender.send(message);

            log.info("이메일 인증 코드 발송 완료: {}, 목적: {}", to, purpose);
        } catch (Exception e) {
            log.error("이메일 발송 중 오류 발생: " + to, e);
            throw new TechnicalException("이메일 인증 코드 발송 중 오류가 발생했습니다", e);
        }
    }


}
