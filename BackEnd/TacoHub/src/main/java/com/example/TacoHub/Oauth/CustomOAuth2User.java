package com.example.TacoHub.Oauth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.TacoHub.Dto.AccountDto;

public class CustomOAuth2User implements OAuth2User{
    
    private final AccountDto accountDto;

    public CustomOAuth2User (AccountDto accountDto)
    {
        this.accountDto = accountDto;
    }

    // 
    @Override
    public Map<String, Object> getAttributes() {

        return null;
    }


    // 권한 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {

                return accountDto.getRole();
            }
        });

        return collection;
    }

    // 이메일 반환
    @Override
    public String getName() {
        return accountDto.getEmailId();
    }

}
