package com.example.TacoHub.Dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LogInDto {

    private String emailId; // 이메일 ID
    private String password; // 비밀번호

}
