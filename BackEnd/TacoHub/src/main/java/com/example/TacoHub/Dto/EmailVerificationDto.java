package com.example.TacoHub.Dto;

import jakarta.validation.constraints.Email;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EmailVerificationDto {

    @Email(message = "Invalid email format")
    private String email;

    private String authCode; // 인증 코드


    private String purpose; // 인증 목적 (회원가입, 비밀번호 찾기 등)

}
