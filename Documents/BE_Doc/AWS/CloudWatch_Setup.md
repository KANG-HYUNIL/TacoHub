# AWS CloudWatch 로그 설정 가이드

## 1. 개요

TacoHub의 AWS CloudWatch 로그 연동을 위한 설정 및 배포 가이드입니다. 실제 배포 시 필요한 AWS 리소스 생성, IAM 권한 설정, 그리고 문제 해결 방법을 다룹니다.

## 2. AWS 리소스 사전 준비

### 2.1 CloudWatch 로그 그룹 생성

```bash
# Test 환경 로그 그룹 생성
aws logs create-log-group --log-group-name /aws/ec2/tacohub-test --region ap-northeast-2

# Production 환경 로그 그룹 생성  
aws logs create-log-group --log-group-name /aws/ec2/tacohub-prod --region ap-northeast-2

# 로그 보존 기간 설정 (30일)
aws logs put-retention-policy --log-group-name /aws/ec2/tacohub-prod --retention-in-days 30
```

### 2.2 S3 버킷 생성 및 Lifecycle 설정

```bash
# S3 버킷 생성
aws s3 mb s3://tacohub-audit-logs-prod --region ap-northeast-2

# Lifecycle 정책 적용
cat > lifecycle-policy.json << EOF
{
    "Rules": [
        {
            "ID": "AuditLogLifecycle",
            "Status": "Enabled",
            "Filter": {"Prefix": "audit-logs/"},
            "Transitions": [
                {
                    "Days": 90,
                    "StorageClass": "STANDARD_IA"
                },
                {
                    "Days": 365,
                    "StorageClass": "GLACIER"
                },
                {
                    "Days": 2555,
                    "StorageClass": "DEEP_ARCHIVE"
                }
            ]
        }
    ]
}
EOF

aws s3api put-bucket-lifecycle-configuration \
    --bucket tacohub-audit-logs-prod \
    --lifecycle-configuration file://lifecycle-policy.json
```

### 2.3 IAM 권한 설정

#### EC2 Instance Profile 정책 (권장)

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "logs:CreateLogGroup",
                "logs:CreateLogStream", 
                "logs:PutLogEvents",
                "logs:DescribeLogGroups",
                "logs:DescribeLogStreams"
            ],
            "Resource": "arn:aws:logs:ap-northeast-2:*:log-group:/aws/ec2/tacohub-*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:PutObjectAcl",
                "s3:GetBucketLocation"
            ],
            "Resource": [
                "arn:aws:s3:::tacohub-audit-logs-*",
                "arn:aws:s3:::tacohub-audit-logs-*/*"
            ]
        }
    ]
}
```

## 3. 환경변수 vs EC2 Instance Profile

### 3.1 우선순위 및 동작 방식

AWS SDK의 자격증명 확인 순서:
1. **Java System Properties** (`aws.accessKeyId`, `aws.secretKey`)
2. **환경변수** (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`)  
3. **Web Identity Token credentials** (EKS/Fargate용)
4. **EC2 Instance Profile** (EC2 메타데이터 서비스)
5. **AWS credentials file** (`~/.aws/credentials`)

### 3.2 환경변수 주입 실패 시 대응방안

#### 문제 상황
```bash
# 환경변수가 제대로 로드되지 않는 경우
ERROR: Unable to load AWS credentials from any provider in the chain
```

#### 해결방법 1: EC2 Instance Profile 사용 (권장)

```bash
# 1. IAM Role 생성
aws iam create-role --role-name TacoHubEC2Role --assume-role-policy-document '{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Principal": {"Service": "ec2.amazonaws.com"},
            "Action": "sts:AssumeRole"
        }
    ]
}'

# 2. 정책 연결
aws iam attach-role-policy \
    --role-name TacoHubEC2Role \
    --policy-arn arn:aws:iam::123456789012:policy/TacoHubLoggingPolicy

# 3. Instance Profile 생성 및 연결
aws iam create-instance-profile --instance-profile-name TacoHubProfile
aws iam add-role-to-instance-profile \
    --instance-profile-name TacoHubProfile \
    --role-name TacoHubEC2Role

# 4. EC2에 Instance Profile 연결
aws ec2 associate-iam-instance-profile \
    --instance-id i-1234567890abcdef0 \
    --iam-instance-profile Name=TacoHubProfile
```

#### 해결방법 2: 환경변수 강제 설정

```bash
# /etc/environment에 추가 (시스템 전역)
echo 'AWS_ACCESS_KEY_ID=AKIA...' >> /etc/environment
echo 'AWS_SECRET_ACCESS_KEY=abc123...' >> /etc/environment

# systemd 서비스에서 강제 설정
cat > /etc/systemd/system/tacohub.service << EOF
[Unit]
Description=TacoHub Application
After=network.target

[Service]
Type=simple
User=tacohub
WorkingDirectory=/opt/tacohub
ExecStart=/usr/bin/java -jar tacohub.jar
Environment=AWS_ACCESS_KEY_ID=AKIA...
Environment=AWS_SECRET_ACCESS_KEY=abc123...
Environment=AWS_REGION=ap-northeast-2
Restart=always

[Install]
WantedBy=multi-user.target
EOF
```

#### 해결방법 3: Docker 환경에서 강제 주입

```dockerfile
# Dockerfile에서 환경변수 설정
FROM openjdk:21-jre-slim
COPY tacohub.jar app.jar
ENV AWS_ACCESS_KEY_ID=AKIA...
ENV AWS_SECRET_ACCESS_KEY=abc123...
ENTRYPOINT ["java", "-jar", "/app.jar"]
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
      - AWS_REGION=ap-northeast-2
    # 또는 .env 파일 사용
    env_file:
      - .env.production
```

## 4. 배포별 환경변수 설정 전략

### 4.1 개발 환경
```bash
# .env 파일 사용
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=abc123...
AWS_S3_BUCKET_NAME=tacohub-audit-logs-dev
AWS_CLOUDWATCH_LOG_GROUP_LOCAL=/aws/ec2/tacohub-local
```

### 4.2 CI/CD 환경
```yaml
# GitHub Actions
env:
  AWS_ACCESS_KEY_ID: ${{ secrets.AWS_ACCESS_KEY_ID }}
  AWS_SECRET_ACCESS_KEY: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
  AWS_S3_BUCKET_NAME: tacohub-audit-logs-staging

# Jenkins
environment {
    AWS_ACCESS_KEY_ID = credentials('aws-access-key')
    AWS_SECRET_ACCESS_KEY = credentials('aws-secret-key')
}
```

### 4.3 운영 환경 (EC2)
```bash
# Instance Profile 사용 (자격증명 불필요)
export AWS_S3_BUCKET_NAME=tacohub-audit-logs-production
export AWS_CLOUDWATCH_LOG_GROUP_PROD=/aws/ec2/tacohub-production
export AWS_CLOUDWATCH_BATCH_SIZE_PROD=1000
```

### 4.4 운영 환경 (Kubernetes)
```yaml
# ConfigMap
apiVersion: v1
kind: ConfigMap
metadata:
  name: tacohub-aws-config
data:
  AWS_S3_BUCKET_NAME: "tacohub-audit-logs-k8s"
  AWS_CLOUDWATCH_LOG_GROUP_PROD: "/aws/eks/tacohub-prod"

---
# ServiceAccount with IAM Role (IRSA)
apiVersion: v1
kind: ServiceAccount
metadata:
  name: tacohub-serviceaccount
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::123456789012:role/TacoHubEKSRole
```

## 5. 문제 해결 및 디버깅

### 5.1 CloudWatch 연결 테스트

```bash
# AWS CLI로 연결 확인
aws sts get-caller-identity
aws logs describe-log-groups --region ap-northeast-2

# Java 애플리케이션에서 확인
aws logs put-log-events \
    --log-group-name /aws/ec2/tacohub-test \
    --log-stream-name test-stream \
    --log-events timestamp=$(date +%s000),message="Test message"
```

### 5.2 일반적인 오류 및 해결

#### 오류 1: 자격증명 실패
```
com.amazonaws.SdkClientException: Unable to load AWS credentials
```

**해결책:**
1. AWS CLI 설정 확인: `aws configure list`
2. 환경변수 확인: `echo $AWS_ACCESS_KEY_ID`
3. Instance Profile 확인: `curl http://169.254.169.254/latest/meta-data/iam/security-credentials/`

#### 오류 2: 로그 그룹 없음
```
ResourceNotFoundException: The specified log group does not exist
```

**해결책:**
```bash
# 로그 그룹 생성
aws logs create-log-group --log-group-name /aws/ec2/tacohub-prod --region ap-northeast-2
```

#### 오류 3: 권한 부족
```
AccessDeniedException: User is not authorized to perform: logs:PutLogEvents
```

**해결책:**
IAM 정책에 필요한 권한 추가 (위의 IAM 정책 참조)

### 5.3 디버깅 도구

#### logback 디버그 모드
```xml
<configuration debug="true">
    <!-- 디버그 정보가 콘솔에 출력됨 -->
</configuration>
```

#### AWS SDK 로그 활성화
```yaml
# application.yml
logging:
  level:
    com.amazonaws: DEBUG
    ca.pjer.logback: DEBUG
```

## 6. 성능 최적화

### 6.1 배치 크기 조정

| 환경 | 배치 크기 | 플러시 시간 | 용도 |
|------|-----------|-------------|------|
| Test | 100 | 10초 | 빠른 피드백 |
| Prod (Low) | 500 | 30초 | 일반 운영 |
| Prod (High) | 1000 | 60초 | 고트래픽 |

### 6.2 비용 최적화

```bash
# 로그 보존 기간 단축 (비용 절약)
aws logs put-retention-policy \
    --log-group-name /aws/ec2/tacohub-prod \
    --retention-in-days 7  # 운영: 7일, 감사: 30일

# VPC 엔드포인트 사용 (데이터 전송 비용 절약)
aws ec2 create-vpc-endpoint \
    --vpc-id vpc-12345678 \
    --service-name com.amazonaws.ap-northeast-2.logs
```

이 가이드를 통해 AWS CloudWatch 로그 연동의 기술적 세부사항과 실제 배포 시나리오를 모두 처리할 수 있습니다.
