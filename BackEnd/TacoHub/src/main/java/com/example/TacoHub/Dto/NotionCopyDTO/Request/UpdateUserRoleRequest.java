package com.example.TacoHub.Dto.NotionCopyDTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRoleRequest {
    
    @NotNull(message = "사용자 ID는 필수입니다")
    private String userId;
    
    @NotBlank(message = "역할은 필수입니다")
    @Pattern(regexp = "^(ADMIN|MEMBER|GUEST)$", message = "역할은 ADMIN, MEMBER, GUEST 중 하나여야 합니다")
    private String role;
    
    private String reason; // 선택적 변경 사유
}
