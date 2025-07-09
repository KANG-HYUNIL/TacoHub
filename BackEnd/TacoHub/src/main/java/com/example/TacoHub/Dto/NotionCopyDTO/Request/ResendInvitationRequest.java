package com.example.TacoHub.Dto.NotionCopyDTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResendInvitationRequest {
    
    @NotBlank(message = "이메일은 필수입니다")
    private String email;
    
    private String message; // 선택적 재전송 메시지
}
