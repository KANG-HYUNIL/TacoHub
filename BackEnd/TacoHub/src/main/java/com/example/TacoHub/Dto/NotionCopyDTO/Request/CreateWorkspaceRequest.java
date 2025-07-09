package com.example.TacoHub.Dto.NotionCopyDTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateWorkspaceRequest {
    
    @NotBlank(message = "워크스페이스 이름은 필수입니다")
    @Size(max = 100, message = "워크스페이스 이름은 100자를 초과할 수 없습니다")
    private String name;
}
