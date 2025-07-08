# ObjectMapperConfig

**패키지:** com.example.TacoHub.Config

## 개요
- Jackson ObjectMapper의 공통 설정을 담당하는 Spring @Configuration 클래스입니다.

## 주요 메서드/Bean
- `objectMapper()`: JavaTimeModule 등록, ISO-8601 날짜 포맷 등 공통 설정

## 동작 흐름
- LocalDateTime 등 Java 8 시간 API 지원
- 날짜 포맷, 순환참조 방지 등 공통 옵션 적용

## 활용
- 감사 로그, API 응답 등 JSON 직렬화/역직렬화
