# server.ts

Express HTTP 서버와 Socket.IO WebSocket 서버를 통합 설정하는 핵심 스크립트.

## 주요 역할
- Express 앱 및 HTTP 서버 생성
- Socket.IO 서버 생성 및 이벤트 핸들러 연결
- CORS, 보안(Helmet), 로깅(Morgan) 등 미들웨어 설정
- REST API 엔드포인트 제공
- RabbitMQ 등 외부 서비스 연동

## 주요 함수/변수
| 이름           | 타입/설명                  | 설명                                  |
|----------------|---------------------------|---------------------------------------|
| createServer   | async function            | 서버 생성 및 설정, 에러 핸들링        |
| app            | Express.Application       | Express 앱 인스턴스                   |
| httpServer     | http.Server               | HTTP 서버 인스턴스                    |
| SocketIOServer | Socket.IO 서버            | 실시간 통신 지원                      |

## 사용 예시
```typescript
const server = await createServer();
server.listen(PORT);
```

## 특이사항
- REST API와 WebSocket을 하나의 서버에서 통합 제공
- 보안 및 로깅 미들웨어 기본 적용
- RabbitMQ 등 외부 서비스 연동 확장 가능
