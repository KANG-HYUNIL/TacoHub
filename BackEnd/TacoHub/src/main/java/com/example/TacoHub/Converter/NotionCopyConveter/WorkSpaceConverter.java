package com.example.TacoHub.Converter.NotionCopyConveter;

import com.example.TacoHub.Dto.NotionCopyDTO.WorkSpaceDTO;
import com.example.TacoHub.Entity.NotionCopyEntity.PageEntity;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceEntity;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceUserEntity;

import java.util.List;

public class WorkSpaceConverter {

    public static WorkSpaceDTO toDTO(WorkSpaceEntity workSpaceEntity)
    {
        WorkSpaceDTO dto = WorkSpaceDTO.builder()
                .id(workSpaceEntity.getId())
                .name(workSpaceEntity.getName())
                .createdAt(workSpaceEntity.getCreatedAt())
                .updatedAt(workSpaceEntity.getUpdatedAt())
                .build();

        // TODO : 전부 DTO로 들기? 
        if (workSpaceEntity.getRootPages() != null)
        {
            dto.setRootPageDTOS(PageConverter.toDTOList(workSpaceEntity.getRootPages()));
        }



        return dto;
    }

//    public static WorkSpaceEntity toEntity(WorkSpaceDTO workSpaceDTO)
//    {
//
//    }


}

