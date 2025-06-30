package com.example.TacoHub.Converter.NotionCopyConveter;

import com.example.TacoHub.Dto.NotionCopyDTO.WorkSpaceUserDTO;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceUserEntity;

public class WorkSpaceUserConverter {

    public static WorkSpaceUserDTO toDTO(WorkSpaceUserEntity workSpaceUserEntity) {
        if (workSpaceUserEntity == null) {
            return null;
        }

        return WorkSpaceUserDTO.builder()
                .userEmailId(workSpaceUserEntity.getUser().getEmailId())
                .workspaceId(workSpaceUserEntity.getWorkspace().getId())
                .workspaceRole(workSpaceUserEntity.getWorkspaceRole().name())
                .membershipStatus(workSpaceUserEntity.getMembershipStatus().name())
                .createdAt(workSpaceUserEntity.getCreatedAt())
                .updatedAt(workSpaceUserEntity.getUpdatedAt())
                .build();
    }

}
