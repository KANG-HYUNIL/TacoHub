# WebSocketServer (src/index.ts)

TacoHub WebSocket 서버의 메인 진입점. 환경변수 초기화, 서버 생성, 에러 핸들링 등 핵심 역할을 수행.

## 주요 역할
- 환경변수(.env) 로드 및 초기화
- Express + Socket.IO 기반 HTTP 서버 생성
- 서버 시작 및 포트 리스닝
- 서버 시작 실패 시 에러 로깅 및 프로세스 종료

## 주요 변수/함수
| 이름           | 타입/설명                  | 설명                                  |
|----------------|---------------------------|---------------------------------------|
| PORT           | number/string              | 서버 리스닝 포트 (환경변수 또는 3001) |
| startServer    | async function             | 서버 생성 및 시작, 에러 핸들링        |
| applicationLogger | Logger                   | 서버 상태 및 에러 로깅                |

## 사용 예시
```typescript
// 서버 시작
startServer();
```

## 특이사항
- 서버 시작 실패 시 process.exit(1)로 즉시 종료
- Express와 Socket.IO를 통합하여 실시간 통신 지원
- 환경별 포트 설정 및 로깅 전략 적용
