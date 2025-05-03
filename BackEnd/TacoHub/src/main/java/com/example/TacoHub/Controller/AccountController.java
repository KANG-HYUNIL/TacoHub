package com.example.TacoHub.Controller;


import com.example.TacoHub.Dto.AccountDto;
import com.example.TacoHub.Service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 사용자 계정 관련 API 요청을 처리하는 컨트롤러
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/account")
@Slf4j
public class AccountController {

    private final AccountService accountService;

    /**
     * 회원가입 요청을 처리한다.
     * 
     * @param accountDto 회원 정보를 담은 DTO (이메일, 비밀번호, 이름, 역할 등)
     * @param authCode 이메일 인증 코드
     * @param purpose 인증 목적 (회원가입)
     * @return 회원가입 성공 또는 실패 응답
     */
    @PostMapping("/postSignup/{authCode}/{purpose}")
    @ResponseBody
    public ResponseEntity<?> postSignup(
            AccountDto accountDto,
            @PathVariable String authCode,
            @PathVariable String purpose)
    {
        try{
            accountService.signUp(accountDto, authCode, purpose);
            return new ResponseEntity<>("회원가입 성공", HttpStatus.OK);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 이메일 ID 중복 확인 요청을 처리한다.
     * 
     * @param accountDto 이메일 ID를 포함한 DTO
     * @return 이메일 중복 여부에 따른 응답
     */
    @PostMapping("/postCheckEmailId")
    @ResponseBody
    public ResponseEntity<?> postCheckEmailId(
            AccountDto accountDto)
    {
        String emailId = accountDto.getEmailId();
        boolean exists = accountService.checkEmailId(emailId);

        if (exists)
        {
            return new ResponseEntity<>("이미 존재하는 이메일입니다.", HttpStatus.CONFLICT);
        }
        else
        {
            return new ResponseEntity<>("사용 가능한 이메일입니다.", HttpStatus.OK);
        }
    }

    // emailId로 사용자 리스트 모두 획득

    // password 변경 요청

}
