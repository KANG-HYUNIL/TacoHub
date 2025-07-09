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
public class UserWorkspacesResponse {
    
    private Long userId;
    private String userEmail;
    private int totalWorkspaces;
    private List<WorkspaceResponse> workspaces;
}
