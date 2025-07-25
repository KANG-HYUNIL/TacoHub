# BlockMessageHandler (com.example.TacoHub.consumer.Handler)

블록 메시지(BlockMessage) 수신 시 실제 비즈니스 로직을 처리하는 핸들러 클래스.

## 주요 역할
- BlockMessage 타입 메시지의 작업(생성, 수정, 삭제 등) 처리
- 메시지 내 블록 데이터 및 작업 정보 기반 비즈니스 로직 수행

## 주요 필드/메서드
| 이름         | 타입/설명                  | 설명                                  |
|--------------|---------------------------|---------------------------------------|
| (구현 필요)  |                           | 실제 블록 메시지 처리 로직 구현 필요   |

## 사용 예시
```java
BlockMessageHandler handler = new BlockMessageHandler();
handler.handle(blockMessage);
```

## 특이사항
- 현재 파일이 비어 있으므로, 실제 메시지 처리 로직 구현 시 상세 문서화 필요
- BlockMessage와 연동하여 확장 가능
