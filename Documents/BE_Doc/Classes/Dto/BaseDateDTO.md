# BaseDateDTO

<table>
  <tr><th>패키지</th><td>com.example.TacoHub.Dto</td></tr>
  <tr><th>클래스 설명</th><td>생성일시(createdAt), 수정일시(updatedAt)를 공통으로 포함하는 추상 DTO 클래스.<br>모든 DTO에서 상속받아 일관된 날짜 정보 제공.</td></tr>
</table>

## 필드 상세 (Fields)
<table>
  <tr><th>이름</th><th>타입</th><th>설명</th></tr>
  <tr><td>createdAt</td><td>LocalDateTime</td><td>객체가 생성된 일시. ISO-8601 포맷으로 직렬화됨.</td></tr>
  <tr><td>updatedAt</td><td>LocalDateTime</td><td>객체가 마지막으로 수정된 일시. ISO-8601 포맷으로 직렬화됨.</td></tr>
</table>

## 생성자 (Constructors)
<table>
  <tr><th>생성자</th><th>설명</th></tr>
  <tr><td>BaseDateDTO()</td><td>기본 생성자. Lombok 또는 명시적 생성자 사용 가능.</td></tr>
  <tr><td>BaseDateDTO(createdAt, updatedAt)</td><td>모든 필드를 초기화하는 생성자.</td></tr>
</table>

## 메서드 상세 (Methods)
<table>
  <tr><th>메서드</th><th>설명</th><th>매개변수</th><th>반환값</th></tr>
  <tr>
    <td>getter/setter</td>
    <td>각 필드의 값을 조회/설정하는 메서드. Lombok @Data로 자동 생성.</td>
    <td>각 필드별(LocalDateTime createdAt, ...)</td>
    <td>해당 필드 값</td>
  </tr>
</table>

## 상속 관계 (Inheritance)
<table>
  <tr><th>자식 클래스</th><th>설명</th></tr>
  <tr><td>AccountDto, PageDTO, BlockDTO 등</td><td>생성/수정일시가 필요한 모든 DTO에서 상속</td></tr>
</table>

## 동작 흐름 (Lifecycle)
1. DTO 객체 생성 시 createdAt, updatedAt 값이 할당된다.
2. API 응답 시 ISO-8601 포맷으로 직렬화되어 전달된다.

## 활용 예시 (Usage)
상속 예시:
```java
public class SomeDto extends BaseDateDTO { ... }
```
API 응답 예시:
```json
{
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

## 예외 및 주의사항 (Exceptions & Notes)
- 날짜 포맷은 ISO-8601로 통일되어야 하며, 직렬화/역직렬화 시 포맷 오류에 주의.
