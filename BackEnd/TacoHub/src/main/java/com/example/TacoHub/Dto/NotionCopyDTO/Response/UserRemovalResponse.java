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
public class UserRemovalResponse {
    
    private Long userId;
    private String email;
    private String role;
    private String removedBy;
    private LocalDateTime removedAt;
    private String reason;
    private boolean isSuccess;
}
