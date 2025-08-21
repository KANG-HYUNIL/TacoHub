# PageService

**패키지:** com.example.TacoHub.Service.NotionCopyService

## 개요
Notion 스타일의 페이지 계층 구조를 관리하는 핵심 서비스 클래스입니다. 워크스페이스 내에서 페이지의 생성, 삭제, 조회, 수정, 복사를 담당하며, 부모-자식 관계를 통한 계층형 페이지 구조를 지원합니다.

## 클래스 구조

### 어노테이션
- `@Service`: Spring 서비스 컴포넌트로 등록
- `@RequiredArgsConstructor`: Lombok을 통한 의존성 주입 생성자 자동 생성
- `@Slf4j`: 로깅 기능 제공

### 멤버 변수
- `PageRepository pageRepository`: 페이지 엔티티 데이터 접근 레이어
- `BlockService blockService`: 페이지와 연결된 블록 관리 서비스 (TODO: 구현 예정)
- `WorkSpaceRepository workspaceRepository`: 워크스페이스 엔티티 데이터 접근 레이어
- `String newPageName = "New Page"`: 새 페이지 생성 시 기본 제목

## 주요 메서드



### getPageEntityOrThrow(UUID pageId)
**목적:** 페이지 조회 및 예외 처리 공통화

**반환값:** `PageEntity`

**예외:** `PageNotFoundException`

### handleAndThrowPageException(String methodName, Exception originalException)
**목적:** 공통 예외 처리 및 래핑

**동작:**
1. 원본 예외 정보 로깅
2. PageOperationException으로 래핑하여 재전파

## 비즈니스 로직

### 페이지 계층 구조
- **루트 페이지**: parentPage가 null인 페이지, 워크스페이스에 직접 소속
- **자식 페이지**: parentPage가 있는 페이지, 부모 페이지에 소속
- **양방향 관계**: 부모는 childPages 리스트, 자식은 parentPage 참조

### 워크스페이스 연동
- 모든 페이지는 특정 워크스페이스에 소속
- 부모-자식 페이지는 반드시 동일 워크스페이스에 속해야 함
- 루트 페이지는 워크스페이스의 rootPages 컬렉션에 추가

### 순서 관리
- orderIndex를 통한 자식 페이지 순서 관리
- 현재는 기본값 0, 향후 적절한 순서 계산 로직 추가 예정

## 로깅 및 모니터링

### 비즈니스 이벤트 로깅
- 루트 페이지 생성: `루트 페이지 생성 완료`
- 자식 페이지 생성: `자식 페이지 생성 완료`
- 페이지 삭제: `페이지 삭제 실행` (데이터 손실 경고)

### 에러 로깅
- 페이지 조회 실패: `페이지 조회 실패: ID가 존재하지 않음`
- 메서드별 실패: `{methodName} 실패: type={exceptionType}, message={errorMessage}`

## 보안 고려사항

### 접근 제어
- 워크스페이스 소유권 검증 필요 (향후 구현)
- 페이지 접근 권한 검증 필요 (향후 구현)

### 데이터 무결성
- 부모-자식 페이지 워크스페이스 일치성 검증
- 순환 참조 방지 로직 필요 (향후 구현)

## 성능 최적화

### 지연 로딩
- PageEntity의 연관 관계는 LAZY 로딩으로 설정
- 필요시에만 연관 데이터 로드

### 배치 처리
- 자식 페이지 삭제 시 CASCADE 활용
- 대량 페이지 처리 시 배치 처리 고려 필요

## 확장 계획

### 단기 확장
- copyPage 메서드 구현
- 페이지 제목 제약 조건 검증
- 적절한 orderIndex 계산 로직

### 장기 확장
- 페이지 템플릿 시스템
- 페이지 버전 관리
- 페이지 검색 기능
- 페이지 권한 관리

## 사용 예시



## 의존성 관계
- **상위 의존성**: Controller 레이어
- **하위 의존성**: PageRepository, WorkSpaceRepository, BlockService, PageConverter
- **연관 서비스**: BlockService (블록 관리), WorkSpaceService (워크스페이스 관리)

## 주의사항
1. 페이지 삭제 시 연결된 블록과 자식 페이지들도 함께 삭제됨
2. 부모-자식 페이지는 반드시 동일 워크스페이스에 속해야 함
3. 순환 참조 방지를 위한 추가 검증 로직이 필요함
4. 대용량 페이지 처리 시 성능 고려사항 검토 필요
