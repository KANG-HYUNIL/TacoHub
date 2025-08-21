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
public class InvitationResponse {
    
    private String invitationToken;
    private String invitedEmail;
    private String role;
    private LocalDateTime invitedAt;
    private LocalDateTime expiresAt;
    private String invitedBy;
    private String message;
    private boolean isEmailSent;
}
