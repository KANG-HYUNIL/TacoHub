# BaseDateEntity

<table>
  <tr><th>패키지</th><td>com.example.TacoHub.Entity.BaseDateEntity</td></tr>
  <tr><th>클래스 설명</th><td>모든 엔티티가 상속받는 추상 클래스.<br>생성일시(createdAt), 수정일시(updatedAt)를 자동으로 관리하며 JPA Auditing 기능을 활용한다.</td></tr>
</table>

## 필드 상세 (Fields)
<table>
  <tr><th>이름</th><th>타입</th><th>설명</th></tr>
  <tr><td>createdAt</td><td>LocalDateTime</td><td>엔티티가 처음 생성된 시점. @CreatedDate로 자동 기록.</td></tr>
  <tr><td>updatedAt</td><td>LocalDateTime</td><td>엔티티가 마지막으로 수정된 시점. @LastModifiedDate로 자동 기록.</td></tr>
</table>

## 생성자 (Constructors)
<table>
  <tr><th>생성자</th><th>설명</th></tr>
  <tr><td>BaseDateEntity()</td><td>기본 생성자. JPA 및 Lombok에서 자동 생성.</td></tr>
  <tr><td>BaseDateEntity(createdAt, updatedAt)</td><td>모든 필드를 초기화하는 생성자.</td></tr>
</table>

## 메서드 상세 (Methods)
<table>
  <tr><th>메서드</th><th>설명</th><th>매개변수</th><th>반환값</th></tr>
  <tr>
    <td>getter/setter</td>
    <td>각 필드의 값을 조회/설정하는 메서드. Lombok @Getter/@Setter로 자동 생성.</td>
    <td>LocalDateTime createdAt, updatedAt</td>
    <td>해당 필드 값</td>
  </tr>
</table>

## 상속 관계 (Inheritance)
<table>
  <tr><th>자식 클래스</th><th>설명</th></tr>
  <tr><td>AccountEntity, PageEntity, WorkSpaceEntity, WorkSpaceUserEntity 등</td><td>생성/수정일시가 필요한 모든 엔티티에서 상속</td></tr>
</table>

## JPA Auditing 예시
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseDateEntity {
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

## 동작 흐름 (Lifecycle)
1. 엔티티 생성/수정 시 createdAt, updatedAt 값이 자동으로 할당된다.
2. API 응답 시 ISO-8601 포맷으로 직렬화되어 전달된다.

## 예외 및 주의사항 (Exceptions & Notes)
- JPA Auditing 설정이 활성화되어 있어야 자동 기록이 동작함.
2. **감사 추적**: 데이터 변경 이력 추적을 위한 기반 제공
3. **공통 필드**: 모든 엔티티에 공통으로 필요한 시간 필드 제공

## 데이터베이스 스키마

상속받는 모든 테이블에 다음 컬럼이 자동으로 추가됩니다:

```sql
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

## 사용 예시

```java
@Entity
public class SomeEntity extends BaseDateEntity {
    // 비즈니스 필드들...
    
    // createdAt, updatedAt은 자동으로 상속됨
}
```
