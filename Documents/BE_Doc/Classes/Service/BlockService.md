# BlockService

**패키지:** com.example.TacoHub.Service.NotionCopyService

## 개요
- Notion 스타일의 블록 CRUD 및 관리를 담당하는 서비스 클래스입니다.

## 주요 멤버 및 의존성
- `BlockDocumentRepository blockDocumentRepository`: 블록 데이터 접근

## 주요 메서드
- `createBlock(BlockDTO blockDTO)`: 블록 생성

## 동작 흐름
1. 입력값 검증
2. DTO → Document 변환
3. 블록 저장 및 기본값/순서 설정

## 예시
```java
blockService.createBlock(blockDTO);
```

## 활용
- 페이지 내 블록 생성/관리, Notion 스타일 에디터 구현
