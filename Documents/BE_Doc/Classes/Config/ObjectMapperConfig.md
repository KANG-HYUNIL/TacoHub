# ObjectMapperConfig

<table>
  <tr><th>패키지</th><td>com.example.TacoHub.Config</td></tr>
  <tr><th>어노테이션</th><td>@Configuration</td></tr>
  <tr><th>클래스 설명</th><td>Jackson ObjectMapper의 공통 설정을 담당하는 Spring Configuration 클래스.<br>Java 8 시간 API 지원, ISO-8601 날짜 포맷 등 일관된 JSON 직렬화/역직렬화 환경을 제공한다.</td></tr>
</table>

## 생성자 (Constructors)
<table>
  <tr><th>생성자</th><th>설명</th></tr>
  <tr><td>ObjectMapperConfig()</td><td>기본 생성자. Spring이 자동으로 빈을 생성할 때 사용.</td></tr>
</table>

## 메서드 상세 (Methods)
<table>
  <tr><th>메서드</th><th>설명</th><th>매개변수</th><th>반환값</th></tr>
  <tr>
    <td>objectMapper()</td>
    <td>JavaTimeModule 등록, ISO-8601 날짜 포맷 등 공통 설정을 적용한 ObjectMapper Bean을 생성.<br>LocalDateTime 등 Java 8 시간 API를 지원하며, 일관된 날짜 포맷을 제공한다.</td>
    <td>없음</td>
    <td>ObjectMapper<br>(com.fasterxml.jackson.databind.ObjectMapper)</td>
  </tr>
</table>

## 동작 흐름 (Lifecycle)
1. Spring 컨테이너가 ObjectMapperConfig를 초기화한다.
2. `objectMapper()`가 호출되어 커스텀 ObjectMapper Bean을 생성한다.
3. 감사 로그, API 응답 등에서 ObjectMapper를 DI 받아 JSON 직렬화/역직렬화에 활용한다.

## 활용 예시 (Usage)
- 감사 로그, API 응답, JSON 데이터 직렬화/역직렬화 등.

## 예외 및 주의사항 (Exceptions & Notes)
- 날짜 포맷 및 직렬화 옵션은 서비스 요구사항에 맞게 조정 가능.
