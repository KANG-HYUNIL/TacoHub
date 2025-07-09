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
public class AcceptInvitationRequest {
    
    @NotBlank(message = "초대 토큰은 필수입니다")
    private String invitationToken;
    
    @NotBlank(message = "사용자 이메일은 필수입니다")
    private String email;
}
