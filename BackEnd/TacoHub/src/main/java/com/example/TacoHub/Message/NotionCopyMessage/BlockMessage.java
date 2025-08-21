package com.example.TacoHub.Message.NotionCopyMessage;

import com.example.TacoHub.Dto.NotionCopyDTO.BlockDTO;
import com.example.TacoHub.Enum.NotionCopyEnum.MessageEnum.BlockOperation;
import com.example.TacoHub.Message.BaseMessage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BlockMessage extends BaseMessage{

    private BlockOperation BlockOperation; // 블록 작업 유형 (생성, 수정, 삭제 등)
    private BlockDTO blockDTO; // 블록 데이터 전송 객체
    private String workspaceId; // 작업 공간 ID
    private String userId; // 사용자 ID (작업 수행자)

    @Override
    public String getRoutingKey() {
        // 라우팅 키 생성: "block.{operation}.{workspaceId}"
        return String.format("block.%s.%s", BlockOperation.name().toLowerCase(), workspaceId);
    }

    
}
