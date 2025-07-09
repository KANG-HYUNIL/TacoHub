# CreateWorkspaceRequest

**패키지:** com.example.TacoHub.Dto.NotionCopyDTO.Request

## 개요
워크스페이스 생성 요청을 위한 DTO(Data Transfer Object) 클래스입니다. 클라이언트가 새로운 워크스페이스를 생성할 때 필요한 정보를 서버로 전송하기 위한 데이터 구조를 제공합니다.

## 클래스 구조

### 어노테이션
- `@Data`: Lombok을 통한 getter/setter/toString/equals/hashCode 자동 생성
- `@Builder`: Lombok 빌더 패턴 지원
- `@NoArgsConstructor`, `@AllArgsConstructor`: 기본/전체 생성자 자동 생성

### 특징
- **유효성 검증**: Jakarta Validation 활용한 입력값 검증
- **불변성**: DTO는 데이터 전송 목적으로 설계
- **JSON 지원**: 자동 직렬화/역직렬화

## 필드 구조

### 1. 워크스페이스 이름
```java
@NotBlank(message = "워크스페이스 이름은 필수입니다")
@Size(max = 100, message = "워크스페이스 이름은 100자를 초과할 수 없습니다")
private String name;
```
- **목적**: 생성할 워크스페이스의 이름
- **제약사항**: 
  - 필수 입력 (공백 불허)
  - 최대 100자
- **활용**: 워크스페이스 식별자, 네비게이션 표시

## JSON 예시

### 요청 예시
```json
{
  "name": "프로젝트 Alpha 워크스페이스"
}
```

### 최소 요청
```json
{
  "name": "개인 워크스페이스"
}
```

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
