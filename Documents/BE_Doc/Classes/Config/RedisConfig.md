
# RedisConfig

<table>
  <tr><th>패키지</th><td>com.example.TacoHub.Config</td></tr>
  <tr><th>클래스 설명</th><td>Redis 연결 및 RedisTemplate 설정을 담당하는 Spring @Configuration 클래스.<br>애플리케이션에서 인증코드, 세션, 캐시 등 다양한 Redis 활용을 지원한다.</td></tr>
</table>

## 필드 상세 (Fields)
<table>
  <tr><th>이름</th><th>타입</th><th>설명</th></tr>
  <tr><td>host</td><td>String</td><td>Redis 서버의 호스트 주소. application.yml에서 주입받음.</td></tr>
  <tr><td>port</td><td>int</td><td>Redis 서버의 포트 번호. application.yml에서 주입받음.</td></tr>
  <tr><td>password</td><td>String</td><td>Redis 서버 접속 비밀번호. 보안 강화를 위해 환경변수로 관리.</td></tr>
  <tr><td>authCodeExpiration</td><td>long</td><td>인증코드 만료 시간(초 단위). 인증 관련 기능에서 사용.</td></tr>
</table>

## 생성자 (Constructors)
<table>
  <tr><th>생성자</th><th>설명</th></tr>
  <tr><td>RedisConfig()</td><td>기본 생성자. Spring이 자동으로 빈을 생성할 때 사용.</td></tr>
</table>

## 메서드 상세 (Methods)
<table>
  <tr><th>메서드</th><th>설명</th><th>매개변수</th><th>반환값</th></tr>
  <tr>
    <td>redisConnectionFactory()</td>
    <td>Redis 서버와의 연결을 생성하는 팩토리 Bean을 반환.<br>application.yml의 host, port, password를 사용하여 RedisConnectionFactory를 구성한다.</td>
    <td>없음</td>
    <td>RedisConnectionFactory<br>(org.springframework.data.redis.connection.RedisConnectionFactory)</td>
  </tr>
  <tr>
    <td>redisTemplate()</td>
    <td>Redis 데이터 입출력을 위한 RedisTemplate Bean을 생성.<br>직렬화 방식 및 연결 팩토리를 설정하여 다양한 Redis 작업을 지원한다.</td>
    <td>없음</td>
    <td>RedisTemplate&lt;String, Object&gt;<br>(org.springframework.data.redis.core.RedisTemplate)</td>
  </tr>
</table>

## 동작 흐름 (Lifecycle)
1. application.yml의 Redis 설정값을 읽어 필드에 주입한다.
2. `redisConnectionFactory()`가 호출되어 Redis 연결 팩토리 Bean을 생성한다.
3. `redisTemplate()`이 호출되어 RedisTemplate Bean을 생성한다.
4. 인증코드, 세션, 캐시 등에서 RedisTemplate을 DI 받아 활용한다.

## 활용 예시 (Usage)
- 인증코드 저장/조회, 세션 관리, 캐시 데이터 저장 등 다양한 Redis 기반 기능 구현에 사용.

## 예외 및 주의사항 (Exceptions & Notes)
- Redis 서버 연결 실패 시 예외 발생 가능. host/port/password 설정을 반드시 확인할 것.
- 인증코드 만료 시간(authCodeExpiration)은 보안 정책에 따라 적절히 조정 필요.
