# base-message.types.ts

RabbitMQ 메시지의 기본 구조를 정의하는 인터페이스.
모든 메시지 타입의 공통 필드 제공.

## 주요 필드
| 필드명      | 타입                | 설명                                  |
|------------|---------------------|---------------------------------------|
| messageId  | string              | 메시지 고유 ID(UUID)                   |
| messageType| MessageType         | 메시지 타입(Enum)                     |
| timestamp  | string (ISO)        | 메시지 생성 시각                      |
| metadata   | Record<string, any> | 추가 메타데이터 (선택)                |

## 특이사항
- getRoutingKey는 실제 객체에서 구현 필요
- 모든 메시지 인터페이스에서 상속하여 공통 필드 활용
