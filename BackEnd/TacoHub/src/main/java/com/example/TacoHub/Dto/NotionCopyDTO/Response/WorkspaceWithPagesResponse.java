package com.example.TacoHub.Dto.NotionCopyDTO.Response;

import com.example.TacoHub.Dto.NotionCopyDTO.PageDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceWithPagesResponse {
    
    private UUID workspaceId;
    private String workspaceName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private int totalPages;
    private List<PageDTO> pages;
    private int totalMembers;
}
