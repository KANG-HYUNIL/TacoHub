# CreateWorkspaceRequest

<table>
  <tr><th>패키지</th><td>com.example.TacoHub.Dto.NotionCopyDTO.Request</td></tr>
  <tr><th>클래스 설명</th><td>워크스페이스 생성 요청을 위한 DTO(Data Transfer Object) 클래스.<br>클라이언트가 새로운 워크스페이스를 생성할 때 필요한 정보를 서버로 전송하기 위한 데이터 구조를 제공한다.</td></tr>
</table>

## 필드 상세 (Fields)
<table>
  <tr><th>이름</th><th>타입</th><th>설명</th><th>예시/제약</th></tr>
  <tr><td>name</td><td>String</td><td>생성할 워크스페이스의 이름. 필수 입력, 최대 100자.</td><td>"프로젝트 Alpha 워크스페이스"</td></tr>
</table>

## 생성자 (Constructors)
<table>
  <tr><th>생성자</th><th>설명</th></tr>
  <tr><td>CreateWorkspaceRequest()</td><td>기본 생성자. Lombok 또는 명시적 생성자 사용 가능.</td></tr>
  <tr><td>CreateWorkspaceRequest(name)</td><td>모든 필드를 초기화하는 생성자.</td></tr>
</table>

## 메서드 상세 (Methods)
<table>
  <tr><th>메서드</th><th>설명</th><th>매개변수</th><th>반환값</th></tr>
  <tr>
    <td>getter/setter</td>
    <td>각 필드의 값을 조회/설정하는 메서드. Lombok @Data로 자동 생성.</td>
    <td>String name</td>
    <td>해당 필드 값</td>
  </tr>
</table>

## 동작 흐름 (Lifecycle)
1. 워크스페이스 생성 요청 시 CreateWorkspaceRequest 객체가 생성된다.
2. name 필드에 값이 할당되어 서버로 전송된다.
3. 서버에서 유효성 검증 후 워크스페이스가 생성된다.

## 활용 예시 (Usage)
워크스페이스 생성 요청:
```json
{
  "name": "프로젝트 Alpha 워크스페이스"
}
```
최소 요청:
```json
{
  "name": "개인 워크스페이스"
}
```

## 예외 및 주의사항 (Exceptions & Notes)
- name 필드는 반드시 입력되어야 하며, 공백 및 100자 초과 불가.

### 유효성 검증 실패 예시
```json
// 빈 이름 - 실패
{
  "name": ""
}

// 너무 긴 이름 - 실패  
{
  "name": "가나다라마바사아자차카타파하가나다라마바사아자차카타파하가나다라마바사아자차카타파하가나다라마바사아자차카타파하가나다라마바사"
}
```

## 활용 시나리오

### 1. 개인 워크스페이스 생성
```java
CreateWorkspaceRequest request = CreateWorkspaceRequest.builder()
    .name("개인 노트")
    .build();
```

### 2. 팀 프로젝트 워크스페이스 생성
```java
CreateWorkspaceRequest request = CreateWorkspaceRequest.builder()
    .name("2024 상반기 마케팅 프로젝트")
    .build();
```

## 처리 흐름
1. **클라이언트 요청**: JSON 형태로 워크스페이스 생성 정보 전송
2. **유효성 검증**: `@Valid` 어노테이션을 통한 자동 검증
3. **서비스 처리**: WorkspaceService로 전달하여 워크스페이스 생성
4. **응답 반환**: 생성된 워크스페이스 정보 또는 에러 응답

## 관련 클래스
- **응답**: `WorkspaceResponse`, `ApiResponse<WorkspaceResponse>`
- **엔티티**: `WorkSpaceEntity`
- **서비스**: `WorkSpaceService`
- **컨트롤러**: `WorkspaceController`
