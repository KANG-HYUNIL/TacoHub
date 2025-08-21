# WorkSpaceUserEntity

<table>
  <tr><th>패키지</th><td>com.example.TacoHub.Entity.WorkSpaceUserEntity</td></tr>
  <tr><th>클래스 설명</th><td>워크스페이스 사용자 정보를 담당하는 엔티티(Entity) 클래스.<br>워크스페이스 ID, 사용자 이메일, 역할, 생성일시, 수정일시 등 정보를 저장하며 DB의 workspace_user 테이블과 매핑된다.</td></tr>
</table>

## 필드 상세 (Fields)
<table>
  <tr><th>이름</th><th>타입</th><th>설명</th><th>제약/예시</th></tr>
  <tr><td>id</td><td>UUID</td><td>워크스페이스 사용자 고유 식별자.</td><td>PRIMARY KEY, NOT NULL</td></tr>
  <tr><td>workspaceId</td><td>UUID</td><td>연동된 워크스페이스의 ID.</td><td>NOT NULL</td></tr>
  <tr><td>userEmail</td><td>String</td><td>사용자 이메일.</td><td>NOT NULL, "user@example.com"</td></tr>
  <tr><td>role</td><td>String</td><td>워크스페이스 내 역할.</td><td>"ADMIN", "MEMBER", "GUEST"</td></tr>
  <tr><td>createdAt</td><td>LocalDateTime</td><td>생성 일시.</td><td>"2024-01-15T10:30:00"</td></tr>
  <tr><td>updatedAt</td><td>LocalDateTime</td><td>수정 일시.</td><td>"2024-01-15T10:30:00"</td></tr>
</table>

## 생성자 (Constructors)
<table>
  <tr><th>생성자</th><th>설명</th></tr>
  <tr><td>WorkSpaceUserEntity()</td><td>기본 생성자. JPA 및 Lombok에서 자동 생성.</td></tr>
  <tr><td>WorkSpaceUserEntity(id, workspaceId, userEmail, role, createdAt, updatedAt)</td><td>모든 필드를 초기화하는 생성자.</td></tr>
</table>

## 메서드 상세 (Methods)
<table>
  <tr><th>메서드</th><th>설명</th><th>매개변수</th><th>반환값</th></tr>
  <tr>
    <td>getter/setter</td>
    <td>각 필드의 값을 조회/설정하는 메서드. Lombok @Getter/@Setter로 자동 생성.</td>
    <td>각 필드별(UUID id, ...)</td>
    <td>해당 필드 값</td>
  </tr>
</table>

## 상속 관계 (Inheritance)
<table>
  <tr><th>부모 클래스</th><th>설명</th></tr>
  <tr><td>BaseDateEntity</td><td>생성일시(createdAt), 수정일시(updatedAt) 자동 관리.</td></tr>
</table>

## 동작 흐름 (Lifecycle)
1. 워크스페이스 사용자 추가/조회/수정 등에서 WorkSpaceUserEntity 객체가 생성된다.
2. 각 필드에 값이 할당되어 DB에 저장된다.
3. Entity-DTO 변환, 비즈니스 로직 처리 등에 활용된다.

## DB 테이블 예시 (Schema)
```sql
CREATE TABLE workspace_user (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    role VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

## 예외 및 주의사항 (Exceptions & Notes)
- workspaceId, userEmail 등 필수값 누락 시 DB 저장이 거부될 수 있음.
- role 값은 시스템 정책에 따라 제한될 수 있음.
