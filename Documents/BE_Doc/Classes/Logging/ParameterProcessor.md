# ParameterProcessor

**패키지:** com.example.TacoHub.Logging

## 개요
- AOP로 캡처한 메서드 파라미터를 안전하게 가공/마스킹/JSON 변환하는 유틸리티 클래스입니다.

## 주요 멤버 및 의존성
- `ObjectMapper objectMapper`: JSON 변환

## 주요 메서드
- `extractParameters(MethodSignature, Object[])`: 파라미터명-값 매핑 및 마스킹
- `maskSensitiveData(String, Object)`: 민감정보(비밀번호, 토큰 등) 마스킹

## 동작 흐름
1. 파라미터명-값을 Map으로 매핑
2. 민감정보 자동 마스킹
3. 복잡한 객체는 JSON 직렬화
4. 직렬화 실패 시 fallback 처리

## 예시
```java
Map<String, Object> params = parameterProcessor.extractParameters(signature, args);
```

## 활용
- 감사 로그 입력 파라미터 안전 기록
- 개인정보/보안 정보 보호
