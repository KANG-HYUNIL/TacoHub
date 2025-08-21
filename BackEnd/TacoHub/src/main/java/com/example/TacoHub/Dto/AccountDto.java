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
    private String provider; // OAuth 제공자 (google, naver 등), 일반 회원가입은 null
    private String oauthId; // OAuth 제공자에서 발급한 고유 ID, 일반 회원가입은 null

}
