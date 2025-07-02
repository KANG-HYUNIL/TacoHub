package com.example.TacoHub.Dto.NotionCopyDTO;

import com.example.TacoHub.Dto.BaseDateDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PageDTO extends BaseDateDTO {

    //기본 Page 정보들
    private UUID id;
    private String title; // Page 제목
    private String path; // Page 경로
    private UUID blockId; // Page에 속한 Block ID
    private Integer orderIndex; // Page의 순서 인덱스
    private Boolean isRoot; // Page가 루트 페이지인지 여부 (서브 페이지가 아닌 경우)

    //속한 workspace 정보

    private UUID workspaceId; // Page가 속한 WorkSpace ID
    private String workspaceName; // Page가 속한 WorkSpace 이름

    //Tree 구조 부모 자식 정보
    private UUID parentPageId; // 부모 Page ID (서브 페이지인 경우)
    private List<PageDTO> childPages; // 자식 Page 리스트 (서브 페이지들)


}
