package com.example.TacoHub.Utils.Jwt;

import com.example.TacoHub.Entity.AccountEntity;
import com.example.TacoHub.Repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security의 인증 과정에서 사용자 정보를 데이터베이스에서 조회하는 서비스
 * UserDetailsService 인터페이스를 구현하여 loadUserByUsername 메소드를 오버라이드
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    /**
     * 사용자명(이메일)으로 사용자 정보를 조회하여 UserDetails 객체로 변환
     *
     * @param username 사용자명(이메일)
     * @return 사용자 정보가 담긴 UserDetails 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 경우 발생
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 데이터베이스에서 사용자 조회
        AccountEntity accountEntity = accountRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("ID가 존재하지 않습니다: " + username));
        
        // CustomUserDetails 객체 생성 및 반환
        return new CustomUserDetails(accountEntity);
    }
}
