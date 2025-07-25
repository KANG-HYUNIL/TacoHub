# SecurityConfig

<table>
  <tr><th>패키지</th><td>com.example.TacoHub.Config</td></tr>
  <tr><th>어노테이션</th><td>@Configuration</td></tr>
  <tr><th>클래스 설명</th><td>Spring Security 인증/인가, JWT 필터, 패스워드 인코더 등 보안 설정을 담당하는 Spring Configuration 클래스.<br>API 인증/인가, JWT 기반 보안 처리, 세션 정책 등 다양한 보안 기능을 제공한다.</td></tr>
</table>

## 필드 상세 (Fields)
<table>
  <tr><th>이름</th><th>타입</th><th>설명</th></tr>
  <tr><td>authenticationConfiguration</td><td>AuthenticationConfiguration</td><td>Spring Security 인증 매니저 설정 객체. 인증 매니저 Bean 생성에 사용.</td></tr>
  <tr><td>redisService</td><td>RedisService</td><td>Redis 기반 인증/세션/토큰 관리 서비스.</td></tr>
  <tr><td>jwtUtil</td><td>JwtUtil</td><td>JWT 토큰 생성/검증/파싱 유틸리티.</td></tr>
</table>

## 생성자 (Constructors)
<table>
  <tr><th>생성자</th><th>설명</th></tr>
  <tr><td>SecurityConfig()</td><td>기본 생성자. Spring이 자동으로 빈을 생성할 때 사용.</td></tr>
</table>

## 메서드 상세 (Methods)
<table>
  <tr><th>메서드</th><th>설명</th><th>매개변수</th><th>반환값</th></tr>
  <tr>
    <td>authenticationManager(authenticationConfiguration)</td>
    <td>Spring Security 인증 매니저 Bean을 생성.<br>사용자 인증 및 권한 검증에 사용된다.</td>
    <td>authenticationConfiguration: 인증 매니저 설정 객체</td>
    <td>AuthenticationManager<br>(org.springframework.security.authentication.AuthenticationManager)</td>
  </tr>
  <tr>
    <td>webSecurityCustomizer()</td>
    <td>특정 경로에 대한 보안 필터 적용 제외 설정.<br>정적 리소스, API 문서 등 보안이 필요 없는 경로를 지정한다.</td>
    <td>없음</td>
    <td>WebSecurityCustomizer<br>(org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer)</td>
  </tr>
  <tr>
    <td>passwordEncoder()</td>
    <td>비밀번호 암호화를 위한 PasswordEncoder Bean을 생성.<br>BCrypt 등 강력한 해시 알고리즘을 적용한다.</td>
    <td>없음</td>
    <td>PasswordEncoder<br>(org.springframework.security.crypto.password.PasswordEncoder)</td>
  </tr>
  <tr>
    <td>securityFilterChain(http)</td>
    <td>JWT 필터, 세션 정책 등 보안 필터 체인을 구성.<br>API 인증/인가, 토큰 검증, 세션 관리 등 다양한 보안 정책을 적용한다.</td>
    <td>http: HttpSecurity 객체</td>
    <td>SecurityFilterChain<br>(org.springframework.security.web.SecurityFilterChain)</td>
  </tr>
</table>

## 동작 흐름 (Lifecycle)
1. application.yml 및 DI를 통해 인증/보안 관련 설정값을 주입받는다.
2. 각 메서드가 호출되어 인증 매니저, 필터 체인, 암호화 등 보안 Bean을 생성한다.
3. API 인증/인가, JWT 토큰 검증, 세션 관리 등에서 각 Bean을 DI 받아 활용한다.

## 활용 예시 (Usage)
- API 인증/인가, JWT 기반 보안 처리, 세션 정책 적용 등.

## 예외 및 주의사항 (Exceptions & Notes)
- 인증/인가 정책, 필터 체인 설정은 서비스 요구사항에 맞게 조정 필요.
- 암호화 알고리즘(PasswordEncoder)은 보안 수준에 따라 선택.
