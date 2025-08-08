package com.example.TacoHub.Controller;

import com.example.TacoHub.Utils.Jwt.TokenReissueService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import com.example.TacoHub.Dto.NotionCopyDTO.Response.ApiResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class JwtController {
    private final TokenReissueService tokenReissueService;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        tokenReissueService.reissueRefreshToken(request, response);
        return ResponseEntity.ok(ApiResponse.success("토큰 재발급 성공", null));
    }
}
