audit:
audit:
# AuditLogConfig

<table>
  <tr><th>패키지</th><td>com.example.TacoHub.Config</td></tr>
  <tr><th>어노테이션</th><td>@Configuration, @EnableAsync</td></tr>
  <tr><th>클래스 설명</th><td>TacoHub의 감사 로그 시스템 구성을 담당하는 Spring Configuration 클래스.<br>환경별로 로그 저장 전략을 동적으로 선택하며, 비동기 처리를 위한 스레드 풀을 설정한다.</td></tr>
</table>

## 필드 상세 (Fields)
<table>
  <tr><th>이름</th><th>타입</th><th>설명</th></tr>
  <tr><td>storageType</td><td>String</td><td>감사 로그 저장 방식 선택(multi, file, s3-archive 등). application.yml에서 주입받음.</td></tr>
</table>

## 생성자 (Constructors)
<table>
  <tr><th>생성자</th><th>설명</th></tr>
  <tr><td>AuditLogConfig()</td><td>기본 생성자. Spring이 자동으로 빈을 생성할 때 사용.</td></tr>
</table>

## 메서드 상세 (Methods)
<table>
  <tr><th>메서드</th><th>설명</th><th>매개변수</th><th>반환값</th></tr>
  <tr>
    <td>auditLogService(fileAuditLogService, s3AuditLogService, multiAuditLogService)</td>
    <td>환경 설정(storageType)에 따라 적합한 감사 로그 서비스(Multi, File, S3)를 반환.<br>CloudWatch, S3, 파일 저장 전략을 동적으로 선택한다.</td>
    <td>
      <ul>
        <li>fileAuditLogService: File 기반 감사 로그 서비스</li>
        <li>s3AuditLogService: S3 기반 감사 로그 서비스</li>
        <li>multiAuditLogService: 복합(Multi) 감사 로그 서비스</li>
      </ul>
    </td>
    <td>AuditLogService<br>(com.example.TacoHub.Logging.AuditLogService)</td>
  </tr>
  <tr>
    <td>auditLogExecutor()</td>
    <td>감사 로그 비동기 처리를 위한 스레드 풀(Executor) Bean을 생성.<br>core/max pool size, queue capacity, thread prefix 등 세부 설정 포함.</td>
    <td>없음</td>
    <td>Executor<br>(java.util.concurrent.Executor)</td>
  </tr>
</table>

## 동작 흐름 (Lifecycle)
1. application.yml의 storageType 설정값을 읽어 필드에 주입한다.
2. `auditLogService(...)`가 호출되어 환경에 맞는 감사 로그 서비스 Bean을 반환한다.
3. `auditLogExecutor()`가 호출되어 비동기 로그 처리를 위한 스레드 풀을 생성한다.
4. 감사 로그 기록 시 비동기 스레드 풀을 통해 처리된다.

## 활용 예시 (Usage)
- CloudWatch+S3+File 동시 저장, 대용량 로그의 안정적 처리, 운영/개발 환경별 저장 전략 분리 등.

## 예외 및 주의사항 (Exceptions & Notes)
- storageType 설정값이 잘못되면 기본값(file)로 동작한다.
- 비동기 처리 시 스레드 풀 설정(core/max/queue 등)을 환경에 맞게 조정 필요.
  log:
    storage:
      type: multi
```

### 6.2 빈 주입 및 사용

```java
@Service
public class SomeService {
    
    private final AuditLogService auditLogService;  // @Primary로 자동 선택
    
    public void someMethod() {
        AuditLog log = AuditLog.builder()...
        auditLogService.save(log);  // 설정된 구현체에 따라 동작
    }
}
```

## 7. 설계 원칙

### 7.1 Strategy Pattern
- 다양한 저장 전략을 런타임에 선택
- 새로운 저장 방식 추가 시 확장 용이

### 7.2 Factory Pattern
- `auditLogService()` 메서드가 팩토리 역할
- 설정에 따라 적절한 구현체 반환

### 7.3 Configuration as Code
- 환경변수를 통한 설정 주입
- 코드 변경 없이 동작 방식 변경 가능

## 8. 주의사항

### 8.1 @Primary 어노테이션
- 여러 `AuditLogService` 구현체 중 기본 빈을 지정
- 다른 구현체 클래스에서는 `@Primary` 제거 필요

### 8.2 비동기 처리
- `@EnableAsync`로 비동기 처리 활성화
- 메인 스레드에 영향 주지 않도록 별도 스레드 풀 사용

### 8.3 Graceful Shutdown
- `setWaitForTasksToCompleteOnShutdown(true)`: 진행 중인 작업 완료 대기
- `setAwaitTerminationSeconds(30)`: 최대 30초 대기

이 클래스는 TacoHub 감사 로깅 시스템의 중추 역할을 하며, 환경별 요구사항에 따라 유연하게 동작합니다.
