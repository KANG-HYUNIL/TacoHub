package com.example.TacoHub.Converter.NotionCopyConveter;

import com.example.TacoHub.Dto.NotionCopyDTO.PageDTO;
import com.example.TacoHub.Entity.NotionCopyEntity.PageEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PageConverter {

    // TODO : childPages를 전부 다 DTO로 들고 가는게 맞을까?
    public static PageDTO toDTO(PageEntity pageEntity)
    {
        PageDTO dto = PageDTO.builder()
                .id(pageEntity.getId())
                .title(pageEntity.getTitle())
                .path(pageEntity.getPath())
                .orderIndex(pageEntity.getOrderIndex())
                .isRoot(pageEntity.getIsRoot())
                .workspaceId(pageEntity.getWorkspace().getId())
                .workspaceName(pageEntity.getWorkspace().getName())
                .parentPageId(pageEntity.getParentPage() != null ? pageEntity.getParentPage().getId() : null)
                .createdAt(pageEntity.getCreatedAt())
                .updatedAt(pageEntity.getUpdatedAt())
                .childPages(toDTOList(pageEntity.getChildPages()))
                .build();

        return dto;
    }

    // TODO : 모든 Page Tree 구조를 다 변환하는게 아니라, 바로 아래 자식들 까지만 변환해서 가져오는건?
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

