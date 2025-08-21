# RedisService

**패키지:** com.example.TacoHub.Service

## 개요
- Redis 데이터 저장/조회/삭제 등 유틸리티 기능을 제공하는 서비스 클래스입니다.

## 주요 멤버 및 의존성
- `RedisTemplate<String, T> redisTemplate`: Redis 연동

## 주요 메서드
- `setValues(String key, T value, Duration duration)`: 데이터 저장
- `getValues(String key)`: 데이터 조회
- `checkExistsValue(String key)`: 키 존재 여부 확인

## 동작 흐름
1. Redis에 데이터 저장(만료시간 설정 가능)
2. 키로 데이터 조회/존재 확인
3. 데이터 삭제

## 예시
```java
redisService.setValues("authCode", "123456", Duration.ofMinutes(5));
String code = redisService.getValues("authCode");
```

## 활용
- 인증코드, 세션, 임시 데이터 등 다양한 캐시/임시 저장소로 활용
