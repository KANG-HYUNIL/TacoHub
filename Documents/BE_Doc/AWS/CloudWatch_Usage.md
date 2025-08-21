# TacoHub 프로젝트의 AWS CloudWatch 로그 사용 내역

## 1. 도입 배경 및 목적
- 운영/테스트 환경에서 서비스의 주요 이벤트, 에러, 감사(AUDIT) 로그를 중앙에서 집계/분석하기 위해 CloudWatch를 도입함.
- 로그 그룹별로 목적을 분리하여, 장애 대응, 보안 감사, 운영 모니터링을 효율적으로 수행.

## 2. 실제 사용 로그 그룹 및 목적
- `/aws/tacohub/test/audit` : 테스트 환경 감사 로그 (AOP 기반, 주요 행위 추적)
- `/aws/tacohub/test/error` : 테스트 환경 에러 로그 (예외, 장애 추적)
- `/aws/tacohub/test/info` : 테스트 환경 정보/경고 로그 (운영 상태 모니터링)
- `/aws/tacohub/test/application` : 테스트 환경 전체 애플리케이션 로그 (백업/분석용)
- `/aws/tacohub/prod/audit` : 운영 환경 감사 로그 (보안/행위 추적)
- `/aws/tacohub/prod/error` : 운영 환경 에러 로그 (장애/예외 추적)
- `/aws/tacohub/prod/info` : 운영 환경 정보/경고 로그 (운영 상태 모니터링)
- `/aws/tacohub/prod/application` : 운영 환경 전체 애플리케이션 로그 (백업/분석용)

## 3. CloudWatch 연동 방식 및 구현
- `logback-spring.xml`에서 환경별(Spring Profile)로 CloudWatch Appender를 분리 설정
    - `test`/`prod` 프로파일에서만 CloudWatch로 로그 전송
    - 각 로그 그룹별로 별도 Appender를 사용하여 목적별 로그 분리
    - JSON 패턴으로 구조화 로그 전송 (분석/검색 용이)
- 감사 로그는 AOP 기반으로 AuditLog 객체를 생성하여, 주요 행위(권한 변경, 초대, 삭제 등)를 구조화하여 기록
- 에러/정보/애플리케이션 로그는 일반 로그와 별도로 CloudWatch에 전송

## 4. 운영/테스트 환경별 동작 차이
- `local` 프로파일: 파일/콘솔 로그만 사용, CloudWatch 미연동
- `test`/`prod` 프로파일: CloudWatch Appender 활성화, 각 로그 그룹으로 분리 전송
- 배치/플러시/블록 타임 등은 Parameter Store에서 관리, 운영 환경에 맞게 동적으로 조정

## 5. 사용 및 개선 과정
- 초기에는 단일 로그 그룹 사용 → 장애/보안/운영 목적별로 그룹 분리
- 감사 로그는 AOP 기반으로 자동화, ParameterProcessor로 민감정보 마스킹/구조화
- 운영 환경에서는 S3 아카이브와 병행하여 장기 보관/비용 최적화
- 장애/성능 이슈 발생 시 로그 그룹/배치/플러시 정책을 Parameter Store에서 즉시 조정

## 6. 참고
- CloudWatch 로그 그룹/스트림/배치 정책은 AWS Parameter Store에서 관리하며, 운영 중 실시간 변경 가능
- 외부 명령어나 AWS 공식 문서 내용은 별도 문서 참고
