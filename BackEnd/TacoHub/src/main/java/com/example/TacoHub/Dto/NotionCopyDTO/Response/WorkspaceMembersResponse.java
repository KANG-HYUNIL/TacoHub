package com.example.TacoHub.Dto.NotionCopyDTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMembersResponse {
    
    private Long workspaceId;
    private String workspaceName;
    private int totalMembers;
    private List<WorkspaceUserResponse> members;
    private List<PendingInvitationResponse> pendingInvitations;
}
