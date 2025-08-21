package com.example.TacoHub.Dto.NotionCopyDTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceUserResponse {
    
    private Long userId;
    private String email;
    private String role;
    private LocalDateTime joinedAt;
    private boolean isActive;
    private LocalDateTime lastActivity;
}
