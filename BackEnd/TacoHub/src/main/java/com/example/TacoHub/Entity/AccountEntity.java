package com.example.TacoHub.Entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "account")
public class AccountEntity {

    @Id
    private String emailId; // 이메일 ID

    @Column
    private String password; // 비밀번호

    @Column
    private String name; //

    @Column
    private String role; // 권한 (ROLE_USER, ROLE_ADMIN)

    @Column(nullable = true)
    private String provider; // OAuth 제공자 (google, naver 등), 일반 회원가입은 null

    @Column(nullable = true)
    private String oauthId; // OAuth 제공자에서 발급한 고유 ID, 일반 회원가입은 null

}
