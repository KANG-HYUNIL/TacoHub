# RedisConfig

**패키지:** com.example.TacoHub.Config

## 개요
- Redis 연결 및 RedisTemplate 설정을 담당하는 Spring @Configuration 클래스입니다.

## 주요 멤버
- `host`, `port`, `password`: Redis 접속 정보
- `authCodeExpiration`: 인증코드 만료 시간

## 주요 메서드/Bean
- `redisConnectionFactory()`: Redis 연결 팩토리
- `redisTemplate()`: RedisTemplate Bean 생성

## 동작 흐름
- application.yml의 Redis 설정값을 읽어 Redis 연결 및 직렬화 방식 지정

## 활용
- 인증코드, 세션, 캐시 등 Redis 활용
