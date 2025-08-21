package com.example.TacoHub.Dto.NotionCopyDTO;

import com.example.TacoHub.Dto.BaseDateDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WorkSpaceUserDTO extends BaseDateDTO {

    // 기본 WorkSpace User 정보
    private UUID workspaceId; // WorkSpace ID
    private String userEmailId; // User 이메일 ID

    // workspace role 정보
    private String workspaceRole; // WorkSpace 내에서의 역할 (예: OWNER, MEMBER 등)

    // membership status 정보
    private String membershipStatus; // WorkSpace 내에서의 멤버십 상태 (예: ACTIVE, INACTIVE 등)
}
