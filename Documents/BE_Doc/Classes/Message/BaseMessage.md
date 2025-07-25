# BaseMessage (com.example.TacoHub.Message)

RabbitMQ 메시지의 기본 구조를 정의하는 추상 클래스.
모든 메시지 타입의 공통 필드와 메서드를 제공하며, 실제 메시지 클래스에서 상속하여 사용.

## 주요 필드
| 필드명      | 타입                | 설명                                  |
|------------|---------------------|---------------------------------------|
| messageId  | String (final)      | 메시지 고유 ID(UUID 자동 생성)         |
| messageType| MessageType         | 메시지 타입(Enum)                     |
| timestamp  | LocalDateTime (final)| 메시지 생성 시각                      |
| metadata   | Map<String, Object> | 추가 메타데이터                       |

## 주요 메서드
| 메서드명         | 반환값/설명                                      |
|------------------|-------------------------------------------------|
| getRoutingKey()  | String - 라우팅 키 생성(구현 필요)               |
| getMessageType() | MessageType - 메시지 타입 반환                   |

## 사용 예시
```java
public class BlockMessage extends BaseMessage {
    // ...
    @Override
    public String getRoutingKey() {
        return "block.update.workspace-123";
    }
}
```

## 특이사항
- 추상 클래스이므로 직접 인스턴스화 불가
- 모든 메시지 클래스에서 상속하여 공통 필드/메서드 활용
