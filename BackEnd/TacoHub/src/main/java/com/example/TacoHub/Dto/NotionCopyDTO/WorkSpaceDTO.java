package com.example.TacoHub.Dto.NotionCopyDTO;

import com.example.TacoHub.Dto.BaseDateDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class WorkSpaceDTO extends BaseDateDTO {

    // 기본 WorkSpace 정보
    private UUID id; // WorkSpace ID
    private String name; // WorkSpace 이름

    // 산하 Page들 정보

    private List<PageDTO> rootPageDTOS; // Page 트리 구조 정보


}
