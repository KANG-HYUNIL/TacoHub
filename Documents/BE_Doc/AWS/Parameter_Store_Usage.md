
# TacoHub 프로젝트의 AWS Parameter Store 사용 내역

## 1. 도입 배경 및 목적
- 운영 환경에서 각종 설정(로그 정책, DB, RabbitMQ, 외부 서비스 등)을 실시간으로 변경/관리하기 위해 AWS Parameter Store를 도입함.
- 환경별(테스트/운영/개발)로 주요 설정값을 동적으로 관리하여, 장애 대응, 보안, 비용 최적화, 운영 편의성에 활용.

## 2. 실제 사용 파라미터 및 목적
### CloudWatch 로그 정책
- `/tacohub/test/cloudwatch/log-group/audit` : 테스트 감사 로그 그룹명
- `/tacohub/test/cloudwatch/batch-size/audit` : 테스트 감사 로그 배치 크기
- `/tacohub/test/cloudwatch/flush-time/audit` : 테스트 감사 로그 플러시 타임
- `/tacohub/prod/cloudwatch/log-group/audit` : 운영 감사 로그 그룹명
- `/tacohub/prod/cloudwatch/batch-size/audit` : 운영 감사 로그 배치 크기
- `/tacohub/prod/cloudwatch/flush-time/audit` : 운영 감사 로그 플러시 타임
- 기타: error/info/application 로그 그룹/배치/플러시 파라미터 등

### DB 설정
- `/tacohub/prod/db/url` : 운영 DB 접속 URL
- `/tacohub/prod/db/username` : 운영 DB 사용자명
- `/tacohub/prod/db/password` : 운영 DB 비밀번호(암호화/마스킹)
- `/tacohub/test/db/url` : 테스트 DB 접속 URL
- `/tacohub/test/db/username` : 테스트 DB 사용자명
- `/tacohub/test/db/password` : 테스트 DB 비밀번호

### RabbitMQ 설정
- `/tacohub/prod/rabbitmq/host` : 운영 RabbitMQ 호스트
- `/tacohub/prod/rabbitmq/username` : 운영 RabbitMQ 사용자명
- `/tacohub/prod/rabbitmq/password` : 운영 RabbitMQ 비밀번호
- `/tacohub/test/rabbitmq/host` : 테스트 RabbitMQ 호스트
- `/tacohub/test/rabbitmq/username` : 테스트 RabbitMQ 사용자명
- `/tacohub/test/rabbitmq/password` : 테스트 RabbitMQ 비밀번호

### 기타 환경설정
- `/tacohub/prod/jwt/access-secret` : 운영 JWT Access Secret
- `/tacohub/prod/jwt/refresh-secret` : 운영 JWT Refresh Secret
- `/tacohub/prod/mail/host` : 운영 메일 서버 호스트
- `/tacohub/prod/mail/username` : 운영 메일 사용자명
- `/tacohub/prod/mail/password` : 운영 메일 비밀번호
- 기타: S3 버킷명, Redis 설정, 외부 API 키 등

## 3. 연동 및 구현 방식
- `application.yml` 및 `logback-spring.xml`에서 각 설정값을 Parameter Store에서 동적으로 로드
    - `${cloudwatch.log-group.audit}` 등으로 파라미터 값을 참조
    - 운영 중 파라미터 변경 시, 재배포 없이 즉시 정책 반영 가능
- Spring Cloud AWS 또는 직접 AWS SDK를 통해 파라미터 값을 주입받음
- 민감정보(비밀번호, 토큰 등)는 암호화/마스킹하여 관리

## 4. 사용 및 개선 과정
- 초기에는 application.yml에 하드코딩 → 운영/테스트 환경 분리 및 Parameter Store로 이전
- 장애/성능/보안 이슈 발생 시 각종 설정을 Parameter Store에서 즉시 조정하여 대응
- CloudWatch, DB, RabbitMQ, 메일 등 주요 설정을 환경별로 세분화하여 관리
- 운영/테스트/개발 환경별로 파라미터를 분리하여, 실수/오용 방지

## 5. 참고
- Parameter Store의 내부 동작/로직은 AWS 공식 문서 참고
- TacoHub 프로젝트에서는 "어떤 파라미터를 왜, 어떻게" 사용하는지에 집중하여 문서화함
