package com.example.TacoHub.Oauth;

import com.example.TacoHub.Dto.AccountDto;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

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

        // AccountDto 생성
        AccountDto accountDto = AccountDto.builder()
                .emailId(oAuth2Response.getEmail())
                .name(oAuth2Response.getName())
                .provider(oAuth2Response.getProvider())
                .oauthId(oAuth2Response.getProviderId())
                .role("ROLE_USER")
                .build();

        // CustomOAuth2User 대신 null 반환 (실제 서비스에서는 적절한 OAuth2User 구현체 반환 필요)
        return null;
    }
}
