# BlockMessage (com.example.TacoHub.Message.NotionCopyMessage)

NotionCopy 블록 작업 메시지 클래스. RabbitMQ를 통해 블록 생성/수정/삭제 등 작업 정보를 전송할 때 사용.

## 주요 필드
| 필드명         | 타입                | 설명                                  |
|---------------|---------------------|---------------------------------------|
| BlockOperation| BlockOperation      | 블록 작업 유형(생성, 수정, 삭제 등)   |
| blockDTO      | BlockDTO            | 블록 데이터 전송 객체                 |
| workspaceId   | String              | 작업 공간 ID                          |
| userId        | String              | 작업 수행자(사용자) ID                |

## 주요 메서드
| 메서드명         | 반환값/설명                                      |
|------------------|-------------------------------------------------|
| getRoutingKey()  | String - 라우팅 키 생성("block.{operation}.{workspaceId}") |

## 사용 예시
```java
BlockMessage msg = BlockMessage.builder()
    .BlockOperation(BlockOperation.CREATE)
    .blockDTO(blockDTO)
    .workspaceId("workspace-123")
    .userId("user-456")
    .build();
String routingKey = msg.getRoutingKey(); // "block.create.workspace-123"
```

## 특이사항
- BaseMessage를 상속하여 공통 필드/메서드 활용
- 라우팅 키는 블록 작업 유형과 워크스페이스 ID 기반 동적 생성
