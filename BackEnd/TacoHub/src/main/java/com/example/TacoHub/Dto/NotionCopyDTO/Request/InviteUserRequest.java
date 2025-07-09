package com.example.TacoHub.Dto.NotionCopyDTO.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteUserRequest {
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "유효한 이메일 형식이어야 합니다")
    private String email;

    @NotBlank(message = "역할은 필수입니다")
    @Pattern(regexp = "^(ADMIN|MEMBER|GUEST)$", message = "역할은 ADMIN, MEMBER, GUEST 중 하나여야 합니다")
    private String role;

    private String message; // 선택적 초대 메시지
    
    private Integer expirationDays; // 선택적 만료일 설정 (기본값: 7일)
}
