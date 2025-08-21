# BlockService

**패키지:** com.example.TacoHub.Service.NotionCopyService

## 1. 개요
`BlockService`는 Notion 스타일의 블록 기반 에디터에서 블록 CRUD 및 계층 구조 관리를 담당하는 핵심 서비스 클래스입니다. 블록의 생성, 수정, 이동, 삭제와 부모-자식 관계 관리, 순서 인덱스 관리 등 복잡한 계층형 데이터 구조를 처리합니다.

## 2. 의존성 및 환경
- **Spring Framework**: @Service, @Transactional, @RequiredArgsConstructor
- **MongoDB**: BlockDocumentRepository (NoSQL 기반 문서 저장)
- **TacoHub Components**: BlockDocument, BlockDTO
- **TacoHub Exceptions**: BlockNotFoundException, BlockOperationException
- **Lombok**: @RequiredArgsConstructor, @Slf4j
- **JDK**: java.util.UUID, java.time.LocalDateTime, java.util.stream

## 3. 클래스 멤버 및 의미

### 3.1 Repository 의존성
#### `blockDocumentRepository` (BlockDocumentRepository, final)
- **의미**: MongoDB 기반 블록 문서 저장소 접근 레포지토리
- **역할**: 블록 데이터의 CRUD 연산, 쿼리 기반 조회
- **주입**: 생성자 주입(final)
- **특징**: MongoDB 특성상 관계형 DB보다 유연한 스키마 지원

## 4. 메서드 상세 설명



### 5.8 `void handleAndThrowBlockException(String methodName, Exception originalException)`
- **역할**: 예외 래핑 및 로깅
- **특징**: 메서드명, 예외 타입, 메시지를 포함한 상세 로깅

## 6. 트랜잭션 관리
- **@Transactional 적용**: 모든 쓰기 연산 (create, update, move, delete)
- **롤백 정책**: RuntimeException 발생 시 자동 롤백
- **일관성 보장**: 부모-자식 관계 업데이트의 원자성 보장

## 7. 로깅 전략
- **단계별 로깅**: 작업 시작/완료 INFO 레벨
- **에러 로깅**: 비즈니스 예외 WARN, 시스템 예외 ERROR
- **디버그 로깅**: 부모-자식 관계 변경 상세 로그

## 8. 예외 처리 체계

### 8.1 비즈니스 예외 (재전파)
- `BlockOperationException`: 필수값 누락, 비즈니스 규칙 위반
- `BlockNotFoundException`: 존재하지 않는 블록 참조

### 8.2 시스템 예외 (래핑)
- MongoDB 연결 오류, 데이터 직렬화 오류 등
- handleAndThrowBlockException()으로 일관된 예외 처리

## 9. 성능 고려사항

### 9.1 인덱스 활용
- pageId, parentId, orderIndex 조합 인덱스 필요
- isDeleted 필드 인덱스로 삭제된 데이터 제외

### 9.2 재귀 삭제 최적화
- 대량 자식 블록 삭제 시 배치 처리 고려
- 깊은 계층 구조에서 스택 오버플로 주의

## 10. 활용 시나리오

### 10.1 블록 에디터 구현



## 11. 확장 가능성
- **블록 타입 확장**: 텍스트, 이미지, 테이블, 코드 등 다양한 타입
- **실시간 협업**: WebSocket 기반 동시 편집
- **버전 관리**: 블록 변경 이력 추적
- **권한 관리**: 블록별 편집 권한 제어
- **캐싱**: 자주 조회되는 페이지 블록 캐싱
