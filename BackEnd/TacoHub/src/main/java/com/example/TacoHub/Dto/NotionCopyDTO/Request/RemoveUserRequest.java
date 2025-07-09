package com.example.TacoHub.Dto.NotionCopyDTO.Request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoveUserRequest {
    
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;
    
    private String reason; // 선택적 제거 사유
}
