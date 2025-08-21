# block-message.types.ts

NotionCopy 블록 작업 메시지 인터페이스. RabbitMQ를 통해 블록 생성/수정/삭제 등 작업 정보를 전송할 때 사용.

## 주요 필드
| 필드명         | 타입                | 설명                                  |
|---------------|---------------------|---------------------------------------|
| blockOperation| BlockOperation      | 블록 작업 유형(생성, 수정, 삭제 등)   |
| blockDTO      | BlockDTO            | 블록 데이터 전송 객체                 |
| workspaceId   | string              | 작업 공간 ID                          |
| userId        | string              | 작업 수행자(사용자) ID                |

## 특이사항
- BaseMessage를 상속하여 공통 필드 활용
- 라우팅 키는 블록 작업 유형과 워크스페이스 ID 기반 동적 생성
