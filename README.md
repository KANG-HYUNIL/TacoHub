# TacoHub

## 프로젝트 소개
TacoHub는 실시간 스터디 그룹 협업 플랫폼으로, 팀원들 간의 효율적인 커뮤니케이션과 협업을 지원합니다.

## 핵심 기능

### 1. 사용자 인증/인가
- JWT 기반 로그인/회원가입
- 권한 분리 (일반 User, 그룹 Leader)

### 2. 스터디 그룹 관리
- 그룹 생성, 초대, 참여, 탈퇴
- 멤버 관리 기능

### 3. 게시판 & 댓글
- 그룹별 공지사항 게시판
- 일반 게시판 및 댓글 기능
- 계층형 댓글 구조

### 4. 실시간 메모
- WebSocket/STOMP 기반 실시간 협업 메모
- 여러 사용자의 동시 편집 지원

### 5. 일정 공유 캘린더
- 그룹 단위 주간/월간 일정 등록
- 일정 수정 및 조회 기능

### 6. 파일 업로드
- 그룹별 파일 업로드 및 다운로드
- 파일 목록 관리

### 7. 출석 체크
- 하루 1회 출석 등록 기능
- Redis TTL 활용한 출석 기록 저장

## 기술 스택

### 프론트엔드
- 순수 JavaScript
- HTML5/CSS3
- Axios
- WebSocket Client

### 백엔드
- Java 21
- Spring Boot 3.x
- Spring Security + JWT
- STOMP/WebSocket

### 데이터베이스
- MySQL


### 캐시/실시간
- Redis (출석 체크 TTL, 실시간 데이터 캐싱)

### 파일 스토리지
- 로컬 저장소 (추후 AWS S3 연동 가능)

### 웹 서버
- Nginx (정적 자원 제공, API 프록시)

### 배포/인프라
- Docker + Docker Compose
- AWS EC2

## 아키텍처 개요

```
[Client (JavaScript)] ←→ [L7 Proxy (Nginx)]
                              ↓
                     ┌────────────────────┐
                     │     Backend API     │ ←→ MySQL
                     │   (Spring Boot)     │ ←→ Redis (출석 TTL)
                     └────────────────────┘
                              ↓
                  [File]     [WebSocket]  
                  Local      Spring + STOMP Broker
```

## 구현 우선순위

1. 유저 인증/가입/팀 가입 (JWT 기반)
2. 그룹 CRUD + 멤버 초대 및 관리
3. 게시판 + 댓글 기능 (JPA + 계층형 구조)
4. 실시간 메모 (WebSocket + STOMP Broker)
5. 일정 캘린더 (프론트 캘린더 라이브러리 + 백엔드 API)
6. 파일 업로드 (Spring Multipart → 로컬 저장)
7. 출석 기록 저장 (Redis에 TTL + 자동 만료)

