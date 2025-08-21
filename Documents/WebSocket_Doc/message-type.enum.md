# message-type.enum.ts

RabbitMQ 및 WebSocket에서 사용되는 메시지 타입을 정의하는 enum.

## 주요 멤버
| 멤버명      | 값         | 설명                      |
| ----------- | ---------- | ------------------------- |
| BLOCK       | 'block'    | 블록 관련 메시지          |
| PAGE        | 'page'     | 페이지 관련 메시지        |
| WORKSPACE   | 'workspace'| 워크스페이스 관련 메시지  |

## 확장성
- PERMISSION, BROADCAST 등 추가 가능
