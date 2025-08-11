package com.example.TacoHub.Oauth;

import com.example.TacoHub.Dto.AccountDto;
import com.example.TacoHub.Entity.AccountEntity;
import com.example.TacoHub.Repository.AccountRepository;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    // AccountRepository 의존성 주입
    private final AccountRepository accountRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println(oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;

        if ("google".equals(registrationId)) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        // OAuth2Response에서 사용자 정보 추출
        String oAuthEmail = oAuth2Response.getEmail();
        String oAuthName = oAuth2Response.getName();
        String oAuthProvider = oAuth2Response.getProvider();
        String oAuthProviderId = oAuth2Response.getProviderId();

        // emailId로 AccountEntity 조회
        Optional<AccountEntity> accountEntityOptional = accountRepository.findByEmailId(oAuthEmail);

        // AccountEntity가 존재하는 경우
        if (accountEntityOptional.isPresent())
        {
            AccountEntity accountEntity = accountEntityOptional.get();
            // 기존 사용자 정보 업데이트
            accountEntity.setName(oAuthName);
            accountEntity.setProvider(oAuthProvider);
            accountEntity.setOauthId(oAuthProviderId);
            accountRepository.save(accountEntity);

                    // AccountDto 생성
            AccountDto accountDto = AccountDto.builder()
                    .emailId(oAuthEmail)
                    .name(oAuthName)
                    .provider(oAuthProvider)
                    .oauthId(oAuthProviderId)
                    .role(accountEntity.getRole())
                    .build();

            //CustomOAuth2User return

            return new CustomOAuth2User(accountDto);
        }
        // AccountEntity가 존재하지 않는 경우
        else
        {
            // AccountEntity 회원가입 처리

            AccountEntity newAccountEntity = AccountEntity.builder()
                    .emailId(oAuthEmail)
                    .name(oAuthName)
                    .provider(oAuthProvider)
                    .oauthId(oAuthProviderId)
                    .role("ROLE_USER") // 기본 역할 설정
                    .build();

            accountRepository.save(newAccountEntity);

            // AccountDto 생성
            AccountDto accountDto = AccountDto.builder()
                    .emailId(oAuthEmail)
                    .name(oAuthName)
                    .provider(oAuthProvider)
                    .oauthId(oAuthProviderId)
                    .role(newAccountEntity.getRole())
                    .build();

            // CustomOAuth2User 반환
            return new CustomOAuth2User(accountDto);
        }

 

    }
}
