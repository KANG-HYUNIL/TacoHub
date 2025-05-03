package com.example.TacoHub.Controller;

import com.example.TacoHub.Dto.EmailVerificationDto;
import com.example.TacoHub.Service.AuthCodeService;
import com.example.TacoHub.Service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이메일 인증 관련 요청을 처리하는 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;
    private final AuthCodeService authCodeService;

    /**
     * 이메일 인증 코드 발송 요청을 처리한다.
     * 
     * @param emailVerificationDto 이메일 주소와 인증 목적을 담은 DTO
     * @return 인증 코드 발송 성공 또는 실패 응답
     */
    @PostMapping("/verification")
    public ResponseEntity<?> sendVerificationCode(@RequestBody EmailVerificationDto emailVerificationDto)
    {
        String email = emailVerificationDto.getEmail();
        String purpose = emailVerificationDto.getPurpose();

        emailService.sendAuthCodeToEmail(email, purpose);
        return ResponseEntity.ok("Verification code sent to " + email);
    }

    /**
     * 이메일 인증 코드 검증 요청을 처리한다.
     * 
     * @param emailVerificationDto 이메일, 인증 코드, 인증 목적을 담은 DTO
     * @return 인증 성공 또는 실패 응답
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestBody EmailVerificationDto emailVerificationDto)
    {
        boolean isVerified = authCodeService.verifyAuthCode(emailVerificationDto);

        if (isVerified) {
            return ResponseEntity.ok("인증 성공");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 실패");
        }
    }

}
