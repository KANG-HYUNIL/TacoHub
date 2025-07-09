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
public class InvitationAcceptResponse {
    
    private String invitationToken;
    private String userEmail;
    private String role;
    private String workspaceName;
    private LocalDateTime acceptedAt;
    private boolean isSuccess;
    private String message;
}
