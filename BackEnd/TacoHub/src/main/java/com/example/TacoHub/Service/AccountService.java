package com.example.TacoHub.Service;

import com.example.TacoHub.Converter.AccountConverter;
import com.example.TacoHub.Dto.AccountDto;
import com.example.TacoHub.Dto.EmailVerificationDto;
import com.example.TacoHub.Entity.AccountEntity;
import com.example.TacoHub.Exception.EmailAlreadyExistsException;
import com.example.TacoHub.Exception.InvalidAuthCodeException;
import com.example.TacoHub.Repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 사용자 계정 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {

    private final AuthCodeService authCodeService;
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private final String ROLE_USER = "ROLE_USER";
    private final String ROLE_ADMIN = "ROLE_ADMIN";
    /**
     * 이메일 ID 중복 검사를 수행한다.
     * 
     * @param emailId 중복 검사할 이메일 ID
     * @return 이메일이 이미 존재하면 true, 존재하지 않으면 false 반환
     */
    public boolean checkEmailId(String emailId) {
        return accountRepository.existsByEmailId(emailId);
    }

    /**
     * 사용자 회원가입을 처리한다.
     * 
     * @param accountDto 회원가입에 필요한 사용자 정보를 담은 DTO
     * @param authCode 이메일 인증 코드
     * @param purpose 인증 목적 (예: 회원가입)
     * @throws EmailAlreadyExistsException 이미 존재하는 이메일인 경우 발생
     * @throws InvalidAuthCodeException 인증 코드가 유효하지 않은 경우 발생
     */
    public void signUp(AccountDto accountDto, String authCode, String purpose) {
        // 이메일 ID 중복 재검사(클라이언트를 믿지마)
        if (checkEmailId(accountDto.getEmailId())) {
            throw new EmailAlreadyExistsException("이미 존재하는 이메일입니다.");
        }

        //
        String email = accountDto.getEmailId();
        EmailVerificationDto emailVerificationDto = new EmailVerificationDto(email, authCode, purpose);

        // 인증 코드 검증
        if (!authCodeService.verifyAuthCode(emailVerificationDto)) {
            throw new InvalidAuthCodeException("인증 코드가 유효하지 않습니다.");
        }

        accountDto.setRole(ROLE_USER); // 기본 역할 설정

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(accountDto.getPassword());
        accountDto.setPassword(encodedPassword);

        AccountEntity accountEntity = AccountConverter.toEntity(accountDto);

        // AccountEntity로 변환 후 저장
        accountRepository.save(accountEntity);
    }
}
