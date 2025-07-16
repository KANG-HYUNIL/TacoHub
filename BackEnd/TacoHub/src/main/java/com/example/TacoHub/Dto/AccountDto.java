package com.example.TacoHub.Dto;


import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {

    private String emailId; // 이메일 ID
    private String password; // 비밀번호
    private String name; // 이름
    private String role; // 권한 (ROLE_USER, ROLE_ADMIN)

}
