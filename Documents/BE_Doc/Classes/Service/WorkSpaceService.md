# WorkSpaceService

**패키지:** com.example.TacoHub.Service.NotionCopyService

## 개요
Notion 스타일의 워크스페이스 관리를 담당하는 핵심 서비스 클래스입니다. 워크스페이스의 생성, 수정, 삭제, 조회를 처리하며, 워크스페이스 생성 시 기본 페이지를 자동으로 생성하는 통합 관리 기능을 제공합니다.

## 클래스 구조

### 어노테이션
- `@Service`: Spring 서비스 컴포넌트로 등록
- `@RequiredArgsConstructor`: Lombok을 통한 의존성 주입 생성자 자동 생성
- `@Slf4j`: 로깅 기능 제공

### 멤버 변수
- `WorkSpaceRepository workspaceRepository`: 워크스페이스 엔티티 데이터 접근 레이어
- `PageService pageService`: 페이지 관리 서비스 (워크스페이스 초기화 시 사용)

## 주요 메서드

### 1. createWorkspaceEntity(String newWorkspaceName)
**목적:** 새로운 워크스페이스 생성 및 초기 설정

**매개변수:**
- `newWorkspaceName`: 생성할 워크스페이스 이름 (필수, 1-100자)

**반환값:** `WorkSpaceEntity` - 생성된 워크스페이스 엔티티

**동작 과정:**
1. 입력값 검증 (null, 빈 문자열, 길이 제한 확인)
2. 이름 공백 제거 (trim)
3. WorkSpaceEntity 빌더 패턴으로 생성
4. 워크스페이스 저장
5. 초기 기본 페이지 생성 (PageService.createPageEntity 호출)
6. 결과 로깅 및 반환

**입력값 검증:**
- null/빈 문자열 체크: `"워크스페이스 이름은 필수입니다"`
- 길이 제한 (100자): `"워크스페이스 이름은 100자를 초과할 수 없습니다"`

**예외 처리:**
- `WorkSpaceOperationException`: 입력값 검증 실패
- 시스템 예외는 래핑하여 `WorkSpaceOperationException`으로 변환

**트랜잭션:** `@Transactional` 적용

**비즈니스 로직:**
- 워크스페이스 생성과 동시에 기본 페이지 자동 생성
- 실패 시 전체 롤백 보장

### 2. editWorkspaceName(String newWorkspaceName, UUID workspaceId)
**목적:** 워크스페이스 이름 변경

**매개변수:**
- `newWorkspaceName`: 새로운 워크스페이스 이름 (필수, 1-100자)
- `workspaceId`: 변경할 워크스페이스 ID (필수)

**동작 과정:**
1. 입력값 검증 (workspaceId, newWorkspaceName null 체크, 길이 제한)
2. 워크스페이스 존재 여부 확인
3. 이름 업데이트 및 저장
4. 결과 로깅

**입력값 검증:**
- `workspaceId` null 체크: `"워크스페이스 ID는 필수입니다"`
- `newWorkspaceName` null/빈 문자열 체크: `"워크스페이스 이름은 필수입니다"`
- 길이 제한 (100자): `"워크스페이스 이름은 100자를 초과할 수 없습니다"`

**예외 처리:**
- `WorkSpaceNotFoundException`: 워크스페이스 미존재
- `WorkSpaceOperationException`: 입력값 검증 실패

**트랜잭션:** `@Transactional` 적용

### 3. deleteWorkspace(UUID workspaceId)
**목적:** 워크스페이스 삭제

**매개변수:**
- `workspaceId`: 삭제할 워크스페이스 ID (필수)

**동작 과정:**
1. 입력값 검증 (workspaceId null 체크)
2. 워크스페이스 존재 여부 확인
3. 워크스페이스 삭제 실행
4. 결과 로깅

**입력값 검증:**
- `workspaceId` null 체크: `"워크스페이스 ID는 필수입니다"`

**예외 처리:**
- `WorkSpaceNotFoundException`: 워크스페이스 미존재
- `WorkSpaceOperationException`: 기타 삭제 실패

**트랜잭션:** `@Transactional` 적용

**주의사항:** 연관된 모든 페이지와 블록도 CASCADE로 삭제됨

### 4. getWorkspaceDto(UUID workspaceId)
**목적:** 워크스페이스 정보 조회 및 DTO 변환

**매개변수:**
- `workspaceId`: 조회할 워크스페이스 ID (필수)

**반환값:** `WorkSpaceDTO` - 워크스페이스 정보 DTO

**동작 과정:**
1. 입력값 검증 (workspaceId null 체크)
2. 워크스페이스 엔티티 조회
3. WorkSpaceConverter를 통한 DTO 변환
4. DTO 반환 및 로깅

**입력값 검증:**
- `workspaceId` null 체크: `"워크스페이스 ID는 필수입니다"`

**예외 처리:**
- `WorkSpaceNotFoundException`: 워크스페이스 미존재
- `WorkSpaceOperationException`: 변환 실패

## Private 헬퍼 메서드

### getWorkSpaceEntityOrThrow(UUID workspaceId)
**목적:** 워크스페이스 조회 및 예외 처리 공통화

**반환값:** `WorkSpaceEntity`

**예외:** `WorkSpaceNotFoundException`

**동작:**
1. Repository에서 workspaceId로 조회
2. 존재하지 않으면 경고 로그와 함께 예외 발생

### handleAndThrowWorkSpaceException(String methodName, Exception originalException)
**목적:** 공통 예외 처리 및 래핑

**동작:**
1. 원본 예외 정보 상세 로깅
2. 메서드명, 예외 타입, 메시지를 포함한 WorkSpaceOperationException으로 래핑
3. 원본 예외를 cause로 설정하여 스택 트레이스 보존

## 비즈니스 로직

### 워크스페이스 생성 플로우
1. 이름 검증 및 정규화 (trim)
2. 워크스페이스 엔티티 생성 및 저장
3. 기본 페이지 자동 생성 (PageService 연동)
4. 전체 프로세스의 트랜잭션 보장

### 데이터 검증 규칙
- **이름 필수성**: null 또는 빈 문자열 불허
- **길이 제한**: 최대 100자
- **공백 처리**: 앞뒤 공백 자동 제거
- **ID 필수성**: 모든 식별자 매개변수 null 체크

### 예외 처리 전략
- **비즈니스 예외**: 직접 전파 (WorkSpaceNotFoundException, WorkSpaceOperationException)
- **시스템 예외**: 래핑하여 WorkSpaceOperationException으로 변환
- **에러 로깅**: 레벨별 차등 적용 (WARN: 비즈니스 오류, ERROR: 시스템 오류)

## 로깅 및 모니터링

### 성공 이벤트 로깅
- 워크스페이스 생성: `워크스페이스 생성 완료: id={}, name={}`
- 이름 변경: `워크스페이스 이름 변경 완료: id={}, newName={}`
- 삭제: `워크스페이스 삭제 완료: id={}`
- 조회: `워크스페이스 조회 완료: id={}`

### 실패 이벤트 로깅
- 입력값 오류: `워크스페이스 생성 실패: 이름이 비어있음`
- 길이 초과: `워크스페이스 생성 실패: 이름이 너무 긺, length={}`
- 미존재: `워크스페이스 조회 실패: ID가 존재하지 않음, id={}`
- 비즈니스 오류: `워크스페이스 생성 비즈니스 오류: {}`
- 시스템 오류: `워크스페이스 생성 시스템 오류: {}`

## 보안 고려사항

### 접근 제어
- 워크스페이스 소유권 검증 필요 (향후 구현)
- 사용자별 워크스페이스 접근 권한 관리 필요

### 데이터 검증
- SQL 인젝션 방지: JPA 파라미터 바인딩 사용
- XSS 방지: 이름 필드 HTML 인코딩 필요 (프론트엔드 또는 컨버터에서)

### 감사 로깅
- 워크스페이스 생성/수정/삭제 이벤트 추적
- 사용자 정보와 연계한 감사 로그 필요

## 성능 최적화

### 데이터베이스 최적화
- 워크스페이스 이름에 인덱스 적용 (검색 성능)
- 삭제 시 CASCADE 성능 모니터링

### 캐싱 전략
- 자주 조회되는 워크스페이스 정보 캐싱 고려
- 워크스페이스 수정 시 캐시 무효화 전략

## 확장 계획

### 단기 확장
- 워크스페이스 템플릿 기능
- 워크스페이스 설정 관리 (공개/비공개, 권한 등)
- 워크스페이스 통계 및 메트릭

### 장기 확장
- 워크스페이스 공유 및 협업 기능
- 워크스페이스 백업/복원 기능
- 워크스페이스 검색 및 필터링
- 워크스페이스 버전 관리

## 사용 예시

### 기본 워크스페이스 관리
```java
// 워크스페이스 생성
WorkSpaceEntity workspace = workSpaceService.createWorkspaceEntity("새 프로젝트");

// 워크스페이스 정보 조회
WorkSpaceDTO dto = workSpaceService.getWorkspaceDto(workspace.getId());

// 워크스페이스 이름 변경
workSpaceService.editWorkspaceName("수정된 프로젝트", workspace.getId());

// 워크스페이스 삭제
workSpaceService.deleteWorkspace(workspace.getId());
```

### 예외 처리
```java
try {
    WorkSpaceEntity workspace = workSpaceService.createWorkspaceEntity("  ");
} catch (WorkSpaceOperationException e) {
    // 입력값 검증 실패 처리
    log.warn("워크스페이스 생성 실패: {}", e.getMessage());
}

try {
    WorkSpaceDTO dto = workSpaceService.getWorkspaceDto(nonExistentId);
} catch (WorkSpaceNotFoundException e) {
    // 워크스페이스 미존재 처리
    log.warn("워크스페이스 조회 실패: {}", e.getMessage());
}
```

## 의존성 관계
- **상위 의존성**: Controller 레이어
- **하위 의존성**: WorkSpaceRepository, PageService, WorkSpaceConverter
- **연관 서비스**: PageService (초기 페이지 생성), WorkSpaceUserService (사용자 관리)

## 주의사항
1. 워크스페이스 삭제 시 모든 연관 데이터(페이지, 블록 등)가 함께 삭제됨
2. 워크스페이스 이름은 100자 제한이 있으며, 앞뒤 공백이 자동 제거됨
3. 워크스페이스 생성 시 기본 페이지가 자동으로 생성되므로 트랜잭션 실패에 주의
4. 대용량 워크스페이스 삭제 시 성능 영향도 고려 필요
5. 향후 사용자 권한 시스템 도입 시 기존 API의 호환성 검토 필요
