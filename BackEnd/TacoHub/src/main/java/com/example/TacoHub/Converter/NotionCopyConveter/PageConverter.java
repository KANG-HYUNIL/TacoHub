package com.example.TacoHub.Converter.NotionCopyConveter;

import com.example.TacoHub.Dto.NotionCopyDTO.PageDTO;
import com.example.TacoHub.Entity.NotionCopyEntity.PageEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PageConverter {

    public static PageDTO toDTO(PageEntity pageEntity)
    {
        PageDTO dto = PageDTO.builder()
                .id(pageEntity.getId())
                .title(pageEntity.getTitle())
                .path(pageEntity.getPath())
                .blockId(pageEntity.getBlockId())
                .workspaceId(pageEntity.getWorkspace().getId())
                .createdAt(pageEntity.getCreatedAt())
                .updatedAt(pageEntity.getUpdatedAt())
                .childPages(toDTOList(pageEntity.getChildPages()))
                .build();

        return dto;
    }


    public static List<PageDTO> toDTOList(List<PageEntity> pageEntities)
    {
        if (pageEntities == null || pageEntities.isEmpty())
        {
            return new ArrayList<PageDTO>(); 
        }

        return pageEntities.stream()
                .map(PageConverter::toDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


}

