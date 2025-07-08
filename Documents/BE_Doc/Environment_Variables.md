# TacoHub 환경변수 관리 가이드

## 1. 개요

TacoHub에서는 보안과 유지보수성을 위해 모든 민감한 정보와 환경별로 다른 값들을 환경변수로 관리합니다.

## 2. 환경변수 사용 구조

### 2.1 설정 파일별 역할

```
.env                     → 개발 환경의 기본값 정의
application.yml          → 환경변수 주입 받아 Spring 설정
logback-spring.xml       → 환경변수로 CloudWatch 설정 동적 구성
```

### 2.2 환경변수 주입 흐름

```
1. .env 파일 → 환경변수 로드
2. application.yml → ${ENV_VAR:default} 문법으로 주입
3. logback-spring.xml → ${ENV_VAR:-default} 문법으로 주입
4. 런타임에서 동적 값 사용
```

## 3. 환경변수 분류

### 3.1 AWS 관련 환경변수

```bash
# 자격증명
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=abc123...
AWS_REGION=ap-northeast-2

# S3 설정
AWS_S3_BUCKET_NAME=tacohub-audit-logs-prod
AWS_S3_BATCH_SIZE=1000
AWS_S3_FLUSH_INTERVAL=300000

# CloudWatch 설정
AWS_CLOUDWATCH_LOG_GROUP_PROD=/aws/ec2/tacohub-production
AWS_CLOUDWATCH_BATCH_SIZE_PROD=500
AWS_CLOUDWATCH_FLUSH_TIME_PROD=30000
AWS_CLOUDWATCH_BLOCK_TIME_PROD=5000
```

### 3.2 애플리케이션 보안 설정

```bash
# JWT 설정
JWT_ACCESS_SECRET=your-super-secret-jwt-access-key-here
JWT_REFRESH_SECRET=your-super-secret-jwt-refresh-key-here
JWT_ACCESS_EXPIRATION=36000000
JWT_REFRESH_EXPIRATION=204800000
```

### 3.3 데이터베이스 설정

```bash
# MySQL
DB_URL_PROD=jdbc:mysql://prod-rds.amazonaws.com:3306/tacohub
DB_USERNAME=admin
DB_PASSWORD=super-secure-password

# Redis
REDIS_HOST_PROD=tacohub-redis.cache.amazonaws.com
REDIS_PORT=6379
REDIS_PASSWORD=redis-secure-password

# MongoDB
MONGODB_URI_PROD=mongodb+srv://user:pass@cluster.mongodb.net/
MONGODB_DATABASE_PROD=tacohub_production
```

## 4. 환경별 설정 전략

### 4.1 로컬 개발 환경

```bash
# .env 파일 사용
AWS_S3_BUCKET_NAME=tacohub-audit-logs-dev
AWS_CLOUDWATCH_LOG_GROUP_LOCAL=/aws/ec2/tacohub-local
DB_URL_LOCAL=jdbc:mysql://localhost:3306/tacohub_dev
```

### 4.2 테스트/스테이징 환경

```bash
# CI/CD에서 환경변수 주입
AWS_S3_BUCKET_NAME=tacohub-audit-logs-staging
AWS_CLOUDWATCH_LOG_GROUP_TEST=/aws/ec2/tacohub-staging
AWS_CLOUDWATCH_BATCH_SIZE_TEST=100
```

### 4.3 운영 환경

```bash
# EC2 Instance Profile 사용 (AWS 자격증명)
# 환경변수 또는 AWS Parameter Store/Secrets Manager 사용
AWS_S3_BUCKET_NAME=tacohub-audit-logs-production
AWS_CLOUDWATCH_LOG_GROUP_PROD=/aws/ec2/tacohub-production
AWS_CLOUDWATCH_BATCH_SIZE_PROD=1000
```

## 5. 배포 환경별 설정 방법

### 5.1 Docker 환경

```dockerfile
# Dockerfile에서
ENV AWS_S3_BUCKET_NAME=tacohub-audit-logs-prod
ENV AWS_CLOUDWATCH_LOG_GROUP_PROD=/aws/ec2/tacohub-prod
```

```yaml
# docker-compose.yml
version: '3.8'
services:
  tacohub:
    image: tacohub:latest
    environment:
      - AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
      - AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
      - AWS_S3_BUCKET_NAME=${AWS_S3_BUCKET_NAME}
      - AWS_CLOUDWATCH_LOG_GROUP_PROD=${AWS_CLOUDWATCH_LOG_GROUP_PROD}
      - JWT_ACCESS_SECRET=${JWT_ACCESS_SECRET}
```

### 5.2 Kubernetes 환경

```yaml
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: tacohub-config
data:
  AWS_S3_BUCKET_NAME: "tacohub-audit-logs-k8s"
  AWS_CLOUDWATCH_LOG_GROUP_PROD: "/aws/eks/tacohub-prod"
  AWS_CLOUDWATCH_BATCH_SIZE_PROD: "800"

---
# secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: tacohub-secrets
type: Opaque
stringData:
  AWS_ACCESS_KEY_ID: "AKIA..."
  AWS_SECRET_ACCESS_KEY: "abc123..."
  JWT_ACCESS_SECRET: "your-jwt-secret"
  JWT_REFRESH_SECRET: "your-refresh-secret"
```

### 5.3 AWS EC2 환경

```bash
# /etc/environment 또는 ~/.bashrc
export AWS_S3_BUCKET_NAME=tacohub-audit-logs-ec2
export AWS_CLOUDWATCH_LOG_GROUP_PROD=/aws/ec2/tacohub-production
export JWT_ACCESS_SECRET=your-production-jwt-secret

# systemd 서비스에서
[Service]
Environment=AWS_S3_BUCKET_NAME=tacohub-audit-logs-prod
Environment=AWS_CLOUDWATCH_LOG_GROUP_PROD=/aws/ec2/tacohub-prod
EnvironmentFile=/opt/tacohub/.env
```

## 6. 보안 모범 사례

### 6.1 민감 정보 관리

```bash
# ✅ 좋은 예: 환경변수 사용
JWT_ACCESS_SECRET=${JWT_ACCESS_SECRET}

# ❌ 나쁜 예: 하드코딩
jwt.access.secret=my-secret-key
```

### 6.2 AWS 자격증명 관리

```bash
# 개발 환경: 환경변수
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=abc123...

# 운영 환경: EC2 Instance Profile (권장)
# AWS IAM Role을 EC2에 연결하여 자격증명 자동 관리
```

### 6.3 환경변수 검증

```yaml
# application.yml에서 필수값 검증
spring:
  config:
    import: "optional:file:.env[.properties]"

jwt:
  access:
    secret: ${JWT_ACCESS_SECRET:#{T(java.util.Objects).requireNonNull(null, 'JWT_ACCESS_SECRET is required')}}
```

## 7. 문제 해결

### 7.1 환경변수가 로드되지 않는 경우

```bash
# 1. .env 파일 위치 확인
ls -la .env

# 2. 환경변수 값 확인
echo $AWS_S3_BUCKET_NAME

# 3. Spring Boot에서 환경변수 확인
curl http://localhost:5000/actuator/env
```

### 7.2 logback에서 환경변수 인식 실패

```xml
<!-- 환경변수 문법 확인 -->
<logGroupName>${AWS_CLOUDWATCH_LOG_GROUP_PROD:-/aws/ec2/tacohub-default}</logGroupName>

<!-- 디버그 모드로 logback 동작 확인 -->
<configuration debug="true">
```

### 7.3 CloudWatch 연결 실패

```bash
# AWS 자격증명 확인
aws sts get-caller-identity

# CloudWatch 로그 그룹 존재 확인
aws logs describe-log-groups --log-group-name-prefix $AWS_CLOUDWATCH_LOG_GROUP_PROD

# IAM 권한 확인
aws iam simulate-principal-policy --policy-source-arn arn:aws:iam::account:role/tacohub-role --action-names logs:PutLogEvents
```

## 8. 배포 체크리스트

### 8.1 개발 → 테스트 배포

- [ ] `.env` 파일의 테스트 환경 값 확인
- [ ] `AWS_CLOUDWATCH_LOG_GROUP_TEST` 설정
- [ ] 테스트 환경 S3 버킷 생성
- [ ] CloudWatch 로그 그룹 생성

### 8.2 테스트 → 운영 배포

- [ ] 운영 환경 환경변수 설정
- [ ] AWS Instance Profile 또는 자격증명 설정
- [ ] 운영 환경 S3 버킷 및 CloudWatch 로그 그룹 생성
- [ ] JWT Secret 키 운영용으로 변경
- [ ] 데이터베이스 연결 정보 운영용으로 변경

이러한 환경변수 관리 전략을 통해 보안성과 유지보수성을 크게 향상시킬 수 있습니다.
