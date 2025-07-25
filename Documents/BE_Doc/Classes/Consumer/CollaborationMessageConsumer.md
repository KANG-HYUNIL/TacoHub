# CollaborationMessageConsumer (com.example.TacoHub.consumer)

RabbitMQ 등 메시지 큐에서 협업 관련 메시지를 수신/처리하는 Consumer 클래스.

## 주요 역할
- 협업 메시지(예: 블록, 페이지, 워크스페이스 등) 수신
- 메시지 타입별 핸들러 호출 및 비즈니스 로직 수행

## 주요 필드/메서드
| 이름         | 타입/설명                  | 설명                                  |
|--------------|---------------------------|---------------------------------------|
| (구현 필요)  |                           | 실제 메시지 수신/처리 로직 구현 필요   |

## 사용 예시
```java
// 메시지 큐에서 메시지 수신 시
CollaborationMessageConsumer consumer = new CollaborationMessageConsumer();
consumer.consume(message);
```

## 특이사항
- 현재 파일이 비어 있으므로, 실제 메시지 처리 로직 구현 시 상세 문서화 필요
- 각 메시지 타입별 핸들러와 연동하여 확장 가능
