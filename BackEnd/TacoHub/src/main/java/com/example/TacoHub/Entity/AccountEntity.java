package com.example.TacoHub.Entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
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

}
