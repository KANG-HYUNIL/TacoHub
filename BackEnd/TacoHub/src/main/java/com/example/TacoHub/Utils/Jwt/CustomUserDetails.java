package com.example.TacoHub.Utils.Jwt;

import com.example.TacoHub.Entity.AccountEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security에서 사용할 사용자 상세 정보 클래스
 * UserDetails 인터페이스를 구현하여 인증 과정에서 사용자 정보를 제공
 */
public class CustomUserDetails implements UserDetails {

    private final AccountEntity accountEntity;

    public CustomUserDetails(AccountEntity accountEntity) {
        this.accountEntity = accountEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 계정이 가진 권한 정보를 GrantedAuthority 객체로 변환하여 반환
        return Collections.singletonList(new SimpleGrantedAuthority(accountEntity.getRole()));
    }

    @Override
    public String getPassword() {
        return accountEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return accountEntity.getEmailId();
    }

    // 계정 관련 상태 체크 메소드들 - 현재 모든 계정은 활성 상태로 간주
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
