# BaseDateEntity

**경로:** `com.example.TacoHub.Entity.BaseDateEntity`

## 개요

모든 엔티티가 상속받는 추상 클래스로, 생성일시와 수정일시를 자동으로 관리합니다. JPA Auditing 기능을 활용하여 데이터의 생성 및 수정 시점을 추적합니다.

## 주요 속성

- **`createdAt`**: 생성 일시
  - 타입: LocalDateTime
  - 어노테이션: @CreatedDate
  - 설명: 엔티티가 처음 생성된 시점을 자동으로 기록

- **`updatedAt`**: 수정 일시  
  - 타입: LocalDateTime
  - 어노테이션: @LastModifiedDate
  - 설명: 엔티티가 마지막으로 수정된 시점을 자동으로 기록

## JPA Auditing 설정

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

## 상속받는 엔티티들

- [AccountEntity](AccountEntity.md)
- [PageEntity](PageEntity.md)
- [WorkSpaceEntity](WorkSpaceEntity.md)
- [WorkSpaceUserEntity](WorkSpaceUserEntity.md)

## 관련 설정

- **Config**: [AuditLogConfig](../Config/AuditLogConfig.md) - JPA Auditing 활성화

## 주요 기능

1. **자동 타임스탬프**: 엔티티 생성/수정 시 자동으로 시간 기록
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
