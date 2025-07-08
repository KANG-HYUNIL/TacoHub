# BaseDateDTO

**패키지:** com.example.TacoHub.Dto

## 개요
- 생성/수정일시를 공통으로 포함하는 추상 DTO 클래스입니다.
- 모든 DTO에서 상속받아 일관된 날짜 정보 제공.

## 주요 멤버
- `createdAt`: 생성 일시 (LocalDateTime)
- `updatedAt`: 수정 일시 (LocalDateTime)

## 예시
```java
public class SomeDto extends BaseDateDTO { ... }
```

## 활용
- 생성/수정일시가 필요한 모든 DTO의 부모 클래스
