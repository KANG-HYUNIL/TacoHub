# SLF4J/Logback CloudWatch 연동 시스템

## 1. 개요

TacoHub의 SLF4J/Logback 기반 CloudWatch 연동 시스템은 애플리케이션의 시스템 로그(에러, 경고, 정보 로그)를 AWS CloudWatch로 실시간 전송하여 운영 모니터링과 알람 기능을 제공합니다. AOP 감사 로깅과는 독립적으로 동작하는 별개의 로깅 경로입니다.

## 2. 시스템 로깅 아키텍처

### 2.1 로깅 경로 및 역할

```
애플리케이션 코드
    ↓
log.error("에러 발생"), log.warn("경고"), log.info("정보")
    ↓
SLF4J Logger (로깅 파사드)
    ↓
Logback (구현체)
    ↓
┌─────────────────┬─────────────────┬─────────────────┐
│   File Appender │  Console        │ CloudWatch      │
│   (로컬 백업)     │  Appender       │ Appender        │
│                 │  (개발용)        │ (운영 모니터링)   │
└─────────────────┴─────────────────┴─────────────────┘
```

**역할 구분:**
- **AOP 감사 로깅**: 비즈니스 로직 추적, 구조화된 감사 데이터
- **SLF4J 시스템 로깅**: 시스템 상태, 오류, 운영 정보 (텍스트 기반)

## 3. Logback 설정 구조

### 3.1 logback-spring.xml 전체 구조

```xml
<configuration>
    <!-- 환경별 프로퍼티 설정 -->
    <springProfile name="local">
        <property name="LOG_LEVEL" value="DEBUG"/>
    </springProfile>
    <springProfile name="test,prod">
        <property name="LOG_LEVEL" value="INFO"/>
    </springProfile>

    <!-- Appender 정의 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <!-- 콘솔 출력 설정 -->
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 파일 출력 설정 -->
    </appender>
    
    <appender name="CLOUDWATCH" class="ca.pjer.logback.AwsLogsAppender">
        <!-- CloudWatch 출력 설정 -->
    </appender>

    <!-- 환경별 Logger 설정 -->
    <springProfile name="local">
        <root level="${LOG_LEVEL}">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
    
    <springProfile name="test,prod">
        <root level="${LOG_LEVEL}">
            <appender-ref ref="FILE"/>
            <appender-ref ref="CLOUDWATCH"/>
        </root>
    </springProfile>
</configuration>
```

### 3.2 CloudWatch Appender 상세 설정

#### Test 환경 (ERROR/WARN만 전송)
```xml
<appender name="CLOUDWATCH_ERROR" class="ca.pjer.logback.AwsLogsAppender">
    <!-- 로그 그룹명 (사전에 생성되어 있어야 함) -->
    <logGroupName>${AWS_CLOUDWATCH_LOG_GROUP_TEST:/aws/ec2/tacohub-test}</logGroupName>
    
    <!-- 로그 스트림 UUID 접두사 (인스턴스별 구분) -->
    <logStreamUuidPrefix>test-instance-</logStreamUuidPrefix>
    
    <!-- 배치 전송 설정 -->
    <maxBatchLogEvents>${AWS_CLOUDWATCH_BATCH_SIZE_TEST:100}</maxBatchLogEvents>
    <maxFlushTimeMillis>${AWS_CLOUDWATCH_FLUSH_TIME_TEST:10000}</maxFlushTimeMillis>
    <maxBlockTimeMillis>2000</maxBlockTimeMillis>
    
    <!-- WARN 이상만 CloudWatch 전송 (비용 최적화) -->
    <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
        <level>WARN</level>
    </filter>
    
    <encoder>
        <pattern>%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
</appender>
```

#### Production 환경 (모든 레벨 전송)
```xml
<appender name="CLOUDWATCH_ALL" class="ca.pjer.logback.AwsLogsAppender">
    <logGroupName>${AWS_CLOUDWATCH_LOG_GROUP_PROD:/aws/ec2/tacohub-prod}</logGroupName>
    <logStreamUuidPrefix>prod-instance-</logStreamUuidPrefix>
    
    <!-- 운영 환경 최적화된 배치 설정 -->
    <maxBatchLogEvents>${AWS_CLOUDWATCH_BATCH_SIZE_PROD:500}</maxBatchLogEvents>
    <maxFlushTimeMillis>${AWS_CLOUDWATCH_FLUSH_TIME_PROD:30000}</maxFlushTimeMillis>
    <maxBlockTimeMillis>5000</maxBlockTimeMillis>
    
    <!-- 필터 없음 = 모든 레벨 허용 (INFO 이상, DEBUG는 ROOT 레벨에서 차단) -->
    
    <encoder>
        <pattern>%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
</appender>
```

## 4. 환경변수 기반 설정

### 4.1 logback-spring.xml에서 환경변수 참조

```xml
<!-- AWS CloudWatch 설정 -->
<logGroupName>${AWS_CLOUDWATCH_LOG_GROUP_PROD:/aws/ec2/tacohub-prod}</logGroupName>
<maxBatchLogEvents>${AWS_CLOUDWATCH_BATCH_SIZE_PROD:500}</maxBatchLogEvents>
<maxFlushTimeMillis>${AWS_CLOUDWATCH_FLUSH_TIME_PROD:30000}</maxFlushTimeMillis>

<!-- 기본값 설정 방식: ${환경변수명:기본값} -->
```

### 4.2 .env 파일 설정

```bash
# CloudWatch 로그 그룹 설정
AWS_CLOUDWATCH_LOG_GROUP_LOCAL=/aws/ec2/tacohub-local
AWS_CLOUDWATCH_LOG_GROUP_TEST=/aws/ec2/tacohub-test  
AWS_CLOUDWATCH_LOG_GROUP_PROD=/aws/ec2/tacohub-prod

# CloudWatch 배치 설정
AWS_CLOUDWATCH_BATCH_SIZE_TEST=100
AWS_CLOUDWATCH_BATCH_SIZE_PROD=500
AWS_CLOUDWATCH_FLUSH_TIME_TEST=10000
AWS_CLOUDWATCH_FLUSH_TIME_PROD=30000
```

## 5. 로그 필터링 및 전송 과정

### 5.1 필터링 메커니즘

#### Test 환경 필터링 과정
```
1. 애플리케이션에서 log.info("사용자 로그인") 호출
   ↓
2. SLF4J Logger가 LoggingEvent 생성
   ↓  
3. Logback이 모든 Appender에게 이벤트 전달
   ↓
4. CloudWatch Appender의 ThresholdFilter 확인
   ↓
5. INFO < WARN 이므로 CloudWatch 전송 거부
   ↓
6. File Appender는 필터 없으므로 파일에 저장

결과: INFO 로그는 파일만 저장, CloudWatch 전송 안됨
```

#### Production 환경 필터링 과정
```
1. 애플리케이션에서 log.info("사용자 로그인") 호출
   ↓
2. SLF4J Logger가 LoggingEvent 생성
   ↓
3. Logback이 모든 Appender에게 이벤트 전달
   ↓
4. CloudWatch Appender에 필터 없음
   ↓
5. File Appender와 CloudWatch Appender 모두 처리

결과: INFO 로그가 파일 저장 + CloudWatch 전송
```

### 5.2 환경별 로그 라우팅 결과

| 로그 레벨 | Local 환경 | Test 환경 | Prod 환경 |
|-----------|------------|-----------|-----------|
| ERROR | Console + File | File + CloudWatch | File + CloudWatch |
| WARN | Console + File | File + CloudWatch | File + CloudWatch |
| INFO | Console + File | File (CloudWatch X) | File + CloudWatch |
| DEBUG | Console + File | 생성되지 않음 | 생성되지 않음 |

## 6. AWS 연결 및 인증

### 6.1 AWS 자격증명 확인 순서

AwsLogsAppender는 다음 순서로 AWS 자격증명을 확인합니다:

```
1. Java System Properties (aws.accessKeyId, aws.secretKey)
   ↓
2. 환경변수 (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
   ↓  
3. Web Identity Token credentials (EKS/Fargate용)
   ↓
4. EC2 Instance Profile (권장)
   ↓
5. AWS credentials file (~/.aws/credentials)
```

### 6.2 초기화 및 연결 과정

```
1. Spring Boot 애플리케이션 시작
   ↓
2. logback-spring.xml 파싱
   ↓
3. AwsLogsAppender 인스턴스 생성
   ↓
4. AWS 자격증명 확인 (위의 순서대로)
   ↓
5. CloudWatch Logs 클라이언트 생성
   ↓
6. 지정된 로그 그룹/스트림 존재 여부 확인
   ↓
7. 애플리케이션 로깅 준비 완료
```

### 6.3 런타임 로그 전송 과정

```
1. 코드에서 log.error("에러 발생") 호출
   ↓
2. SLF4J Logger가 LoggingEvent 생성
   ↓
3. Logback이 모든 Appender에게 이벤트 전달
   ↓
4. CloudWatch Appender의 Filter가 레벨 확인
   ↓
5. 조건 충족 시 내부 버퍼에 로그 이벤트 추가
   ↓
6. 버퍼가 가득 차거나 시간 초과 시 배치 전송
   ↓
7. AWS CloudWatch Logs PutLogEvents API 호출
   ↓
8. CloudWatch에 로그 저장 완료
```

## 7. 실제 사용 예시

### 7.1 Service Layer에서의 로깅

```java
@Service
@Slf4j
public class UserService {
    
    public void createUser(User user) {
        try {
            log.info("사용자 생성 시작: {}", user.getEmail());  // INFO → Prod만 CloudWatch
            
            // 비즈니스 로직
            userRepository.save(user);
            
            log.info("사용자 생성 완료: {}", user.getId());     // INFO → Prod만 CloudWatch
            
        } catch (DataIntegrityViolationException e) {
            log.warn("사용자 생성 실패 - 중복: {}", user.getEmail()); // WARN → Test/Prod 모두 CloudWatch
        } catch (Exception e) {
            log.error("사용자 생성 중 예상치 못한 오류: {}", e.getMessage(), e); // ERROR → Test/Prod 모두 CloudWatch
        }
    }
}
```

### 7.2 환경별 CloudWatch 전송 결과

#### Test 환경에서의 로깅 결과
```java
log.info("사용자 생성 시작");     // 파일만 저장, CloudWatch 전송 안됨
log.warn("중복 사용자");         // 파일 저장 + CloudWatch 전송  
log.error("시스템 오류");        // 파일 저장 + CloudWatch 전송
```

#### Production 환경에서의 로깅 결과
```java
log.info("사용자 생성 시작");     // 파일 저장 + CloudWatch 전송
log.warn("중복 사용자");         // 파일 저장 + CloudWatch 전송
log.error("시스템 오류");        // 파일 저장 + CloudWatch 전송
```

## 8. 비동기 처리 및 성능 최적화

### 8.1 AsyncAppender 사용

```xml
<appender name="ASYNC_CLOUDWATCH" class="ch.qos.logback.classic.AsyncAppender">
    <appender-ref ref="CLOUDWATCH"/>
    <queueSize>512</queueSize>
    <discardingThreshold>0</discardingThreshold>  <!-- 로그 손실 방지 -->
    <includeCallerData>false</includeCallerData>  <!-- 성능 최적화 -->
    <neverBlock>true</neverBlock>                 <!-- 논블로킹 -->
</appender>
```

### 8.2 배치 전송 최적화

#### 트래픽별 최적화 설정
```xml
<!-- 낮은 트래픽 환경 -->
<maxBatchLogEvents>100</maxBatchLogEvents>
<maxFlushTimeMillis>10000</maxFlushTimeMillis>

<!-- 높은 트래픽 환경 -->
<maxBatchLogEvents>1000</maxBatchLogEvents>  
<maxFlushTimeMillis>30000</maxFlushTimeMillis>
```

## 9. 모니터링 및 알람 설정

### 9.1 CloudWatch에서 생성되는 로그 형태

```
2024-01-15T14:30:22.123+09:00 [http-nio-8080-exec-1] ERROR c.e.TacoHub.Service.UserService - 사용자 생성 중 예상치 못한 오류: Connection timeout
java.sql.SQLException: Connection timeout
    at com.mysql.cj.jdbc.ConnectionImpl.connect(ConnectionImpl.java:123)
    at com.example.TacoHub.Service.UserService.createUser(UserService.java:45)
    ...
```

### 9.2 로그 기반 메트릭 필터 설정

#### ERROR 로그 메트릭 생성
```bash
aws logs put-metric-filter \
  --log-group-name /aws/ec2/tacohub-prod \
  --filter-name ErrorLogFilter \
  --filter-pattern "ERROR" \
  --metric-transformations \
    metricName=ErrorCount,metricNamespace=TacoHub/Application,metricValue=1
```

#### 특정 패턴 감지 메트릭
```bash
# 데이터베이스 연결 오류 감지
aws logs put-metric-filter \
  --log-group-name /aws/ec2/tacohub-prod \
  --filter-name DatabaseErrorFilter \
  --filter-pattern "[timestamp, thread, level=ERROR, logger, message=\"*Connection*\"]" \
  --metric-transformations \
    metricName=DatabaseErrors,metricNamespace=TacoHub/Database,metricValue=1
```

### 9.3 CloudWatch 알람 설정

```bash
# ERROR 로그 임계치 알람
aws cloudwatch put-metric-alarm \
  --alarm-name "TacoHub-HighErrorRate" \
  --alarm-description "5분 동안 ERROR 로그 5개 이상 발생 시 알람" \
  --metric-name ErrorCount \
  --namespace TacoHub/Application \
  --statistic Sum \
  --period 300 \
  --threshold 5 \
  --comparison-operator GreaterThanThreshold \
  --evaluation-periods 1 \
  --alarm-actions arn:aws:sns:ap-northeast-2:123456789012:tacohub-alerts
```


## 10. 환경변수 및 Parameter Store 기반 운영 전략

### 10.1 환경변수/Parameter Store 연동

TacoHub의 CloudWatch 연동은 logback-spring.xml에서 환경변수와 AWS Parameter Store 값을 동적으로 참조하여 운영 환경별로 로그 그룹, 배치 크기, flush 시간 등을 관리합니다. 실제 운영에서는 Parameter Store를 통해 로그 그룹명, 배치 설정, 인증 정보 등 주요 값을 안전하게 관리합니다.

#### logback-spring.xml 예시
```xml
<logGroupName>${AWS_CLOUDWATCH_LOG_GROUP_PROD:/aws/ec2/tacohub-prod}</logGroupName>
<maxBatchLogEvents>${AWS_CLOUDWATCH_BATCH_SIZE_PROD:500}</maxBatchLogEvents>
<maxFlushTimeMillis>${AWS_CLOUDWATCH_FLUSH_TIME_PROD:30000}</maxFlushTimeMillis>
```

#### Parameter Store 예시
| Parameter Name | Value (예시) |
|----------------|-----------------------------|
| /tacohub/log/group/prod | /aws/ec2/tacohub-prod |
| /tacohub/log/batch/prod | 500                   |
| /tacohub/log/flush/prod | 30000                 |

### 10.2 운영 전략 요약
- 운영 환경별로 Parameter Store에서 값을 읽어 logback 설정에 반영
- 보안 및 운영 편의성 향상 (코드/설정 분리)
- 환경별 로그 그룹, 배치, flush, 인증 정보 등 모두 Parameter Store에서 관리

## 11. 변경된 로깅 전략 요약

### 11.1 logback 기반 CloudWatch 연동 구조
- 모든 시스템 로그는 SLF4J → Logback → File/CloudWatch Appender 경로로 분리 저장
- 운영 환경에서는 CloudWatch Appender가 모든 INFO 이상 로그를 실시간 전송
- Test 환경에서는 ThresholdFilter로 WARN/ERROR만 전송해 비용 최적화
- 환경변수/Parameter Store로 운영 환경별 설정 동적 관리

### 11.2 실제 사용 예시
...existing code...

### 11.3 운영 전략 및 주의사항
- 운영 환경 변경 시 Parameter Store 값만 변경하면 무중단 설정 변경 가능
- 로그 그룹/스트림은 사전 생성 필요, IAM 권한 필수
- logback 설정 변경 시 반드시 운영 환경별 테스트 필요

## 12. 향후 확장 및 개선 방향

- S3 장기 보관, ElasticSearch/Kafka 연동 등 추가 저장소 확장 가능
- CloudWatch 비용 최적화: 필터링, 보존 기간, VPC 엔드포인트 활용
- 운영 중 장애/트러블슈팅은 실제 발생 시 별도 문서로 관리

---

이 문서는 TacoHub의 CloudWatch 연동 로깅 시스템의 실제 운영 구조와 전략, 환경변수/Parameter Store 기반 설정 관리, logback 기반 변경 사항을 명확히 설명합니다. 미확인 문제, MDC 등 불필요한 내용은 제거하였으며, 실제 운영에 필요한 핵심 정보만을 제공합니다.
