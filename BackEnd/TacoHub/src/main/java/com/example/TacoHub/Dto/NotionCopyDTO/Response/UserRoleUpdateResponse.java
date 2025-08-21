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
public class UserRoleUpdateResponse {
    
    private Long userId;
    private String email;
    private String previousRole;
    private String newRole;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String reason;
}
