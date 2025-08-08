package com.example.TacoHub.Controller;


import com.example.TacoHub.Dto.AccountDto;
import com.example.TacoHub.Service.AccountService;

import jakarta.servlet.http.HttpServletRequest;

import com.example.TacoHub.Dto.NotionCopyDTO.Response.ApiResponse;
import com.example.TacoHub.Exception.BusinessException;
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
    public ResponseEntity<ApiResponse<String>> postSignup(
            AccountDto accountDto,
            @PathVariable String authCode,
            @PathVariable String purpose)
    {
        accountService.signUp(accountDto, authCode, purpose);
        return ResponseEntity.ok(ApiResponse.success("회원가입 성공", null));
    }

    /**
     * 이메일 ID 중복 확인 요청을 처리한다.
     * 
     * @param accountDto 이메일 ID를 포함한 DTO
     * @return 이메일 중복 여부에 따른 응답
     */
    @PostMapping("/postCheckEmailId")
    @ResponseBody
    public ResponseEntity<ApiResponse<String>> postCheckEmailId(
            AccountDto accountDto)
    {
        String emailId = accountDto.getEmailId();
        boolean exists = accountService.checkEmailId(emailId);
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("이미 존재하는 이메일입니다.", "EMAIL_ALREADY_EXISTS"));
        } else {
            return ResponseEntity.ok(ApiResponse.success("사용 가능한 이메일입니다.", null));
        }
    }

    /**
     * Access Token을 요청 헤더로 받아 계정 정보를 반환하는 API
     * @param request HttpServletRequest (헤더에서 access token 추출)
     * @return 계정 정보(AccountDto 등) 또는 에러 응답
     * 내부 로직은 추후 구현 예정
     */
    @PostMapping("/getAccountInfo")
    @ResponseBody
    public ResponseEntity<ApiResponse<AccountDto>> getAccountInfoByAccessToken(HttpServletRequest request) {
        try {
            AccountDto accountDto = accountService.getAccountInfoByAccessToken(request);
            return ResponseEntity.ok(ApiResponse.success("계정 정보 조회 성공", accountDto));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage(), e.getClass().getSimpleName()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("서버 오류", "INTERNAL_ERROR"));
        }
    }

}
