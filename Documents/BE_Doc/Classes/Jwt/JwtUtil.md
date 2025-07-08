# JwtUtil

**패키지:** com.example.TacoHub.Utils.Jwt

## 1. 개요
`JwtUtil`은 JWT(JSON Web Token) 토큰의 생성, 파싱, 검증을 담당하는 핵심 유틸리티 클래스입니다. Access Token과 Refresh Token의 생성과 검증을 통해 TacoHub의 JWT 기반 인증 시스템을 지원합니다.

## 2. 의존성 및 환경
- **JWT 라이브러리**: io.jsonwebtoken (JJWT)
- **Spring Framework**: @Component, @Value
- **JDK**: javax.crypto, java.nio.charset, java.util.Date
- **설정 파일**: application.yml에서 JWT 관련 설정값 주입

## 3. 클래스 멤버 및 의미
### 3.1 `secretKey` (SecretKey, final)
- **의미**: JWT 서명(sign)과 검증에 사용되는 비밀키
- **생성**: HS256 알고리즘 기반으로 String secret을 SecretKeySpec으로 변환
- **보안**: 외부 노출 금지, application.yml에서 관리

### 3.2 `expiredMsAccess` (long, final, @Getter)
- **의미**: Access Token의 만료 시간(밀리초)
- **기본값**: application.yml의 jwt.access.expiration 설정값
- **용도**: 짧은 만료 시간으로 보안성 확보

### 3.3 `expiredMsRefresh` (long, final, @Getter)
- **의미**: Refresh Token의 만료 시간(밀리초)
- **기본값**: application.yml의 jwt.refresh.expiration 설정값
- **용도**: 긴 만료 시간으로 사용자 편의성 확보

## 4. 생성자
### 4.1 `JwtUtil(@Value String secret, @Value long expiredMsAccess, @Value long expiredMsRefresh)`
- **역할**: JWT 유틸리티 객체 초기화 및 비밀키 생성
- **인자**:
  - `secret` (String): JWT 서명용 비밀키 문자열
  - `expiredMsAccess` (long): Access Token 만료 시간(ms)
  - `expiredMsRefresh` (long): Refresh Token 만료 시간(ms)
- **동작**:
  1. secret을 UTF-8로 인코딩하여 SecretKeySpec 생성
  2. HS256 알고리즘으로 secretKey 설정
  3. 만료 시간 멤버 변수 초기화
- **예외**: secret이 null이거나 비어있는 경우 런타임 예외 발생 가능

## 5. 메서드 상세 설명

### 5.1 `String getCategory(String token)`
- **역할**: JWT에서 토큰 카테고리("access" 또는 "refresh") 추출
- **인자**: `token` (String) - 파싱할 JWT 토큰
- **반환값**: `String` - "access" 또는 "refresh"
- **동작**:
  1. JWT 파서로 토큰 검증 및 파싱
  2. Claims에서 "category" 클레임 추출
- **예외**: 토큰이 무효하거나 만료된 경우 JWT 관련 예외 발생

### 5.2 `Boolean isExpired(String token)`
- **역할**: JWT 토큰의 만료 여부 확인
- **인자**: `token` (String) - 확인할 JWT 토큰
- **반환값**: `Boolean` - 만료됨(true), 유효함(false)
- **동작**:
  1. JWT 파서로 토큰 파싱
  2. expiration 클레임과 현재 시간 비교
  3. ExpiredJwtException 캐치 시 true 반환
- **예외**: ExpiredJwtException은 내부에서 처리하여 true 반환

### 5.3 `String getUsername(String token)`
- **역할**: JWT에서 사용자명(이메일) 추출
- **인자**: `token` (String) - 파싱할 JWT 토큰
- **반환값**: `String` - 사용자명(이메일)
- **동작**: Claims에서 "username" 클레임 추출
- **예외**: 토큰이 무효한 경우 JWT 관련 예외 발생

### 5.4 `String getRole(String token)`
- **역할**: JWT에서 사용자 권한(role) 추출
- **인자**: `token` (String) - 파싱할 JWT 토큰
- **반환값**: `String` - 권한 정보(ROLE_USER, ROLE_ADMIN 등)
- **동작**: Claims에서 "role" 클레임 추출
- **예외**: 토큰이 무효한 경우 JWT 관련 예외 발생

### 5.5 `String createAccessJwt(String username, String role)`
- **역할**: Access Token 생성
- **인자**:
  - `username` (String): 사용자명(이메일)
  - `role` (String): 사용자 권한
- **반환값**: `String` - 생성된 JWT Access Token
- **동작**:
  1. "category": "access" 클레임 설정
  2. username, role 클레임 설정
  3. 발행 시간(issuedAt)과 만료 시간(expiration) 설정
  4. secretKey로 서명하여 토큰 생성
- **예외**: 없음 (정상 인자 전달 시)

### 5.6 `String createRefreshJwt(String username, String role)`
- **역할**: Refresh Token 생성
- **인자**:
  - `username` (String): 사용자명(이메일)
  - `role` (String): 사용자 권한
- **반환값**: `String` - 생성된 JWT Refresh Token
- **동작**:
  1. "category": "refresh" 클레임 설정
  2. username, role 클레임 설정
  3. 발행 시간과 만료 시간 설정 (Access Token보다 긴 만료 시간)
  4. secretKey로 서명하여 토큰 생성
- **예외**: 없음 (정상 인자 전달 시)

## 6. 동작 흐름
1. **로그인 시**: LoginFilter에서 createAccessJwt(), createRefreshJwt() 호출하여 토큰 쌍 생성
2. **요청 검증 시**: JwtFilter에서 getCategory(), isExpired() 등으로 토큰 유효성 검증
3. **사용자 정보 추출**: getUsername(), getRole()로 인증된 사용자 정보 추출
4. **토큰 갱신 시**: Refresh Token으로 새로운 Access Token 생성

## 7. 보안 고려사항
- **비밀키 관리**: secretKey는 외부 노출 금지, 충분한 길이와 복잡성 필요
- **토큰 만료 시간**: Access Token은 짧게, Refresh Token은 적절히 설정
- **알고리즘 고정**: HS256 사용으로 일관성 유지
- **클레임 검증**: 토큰 파싱 시 서명 검증 필수

## 8. 활용 시나리오
- **인증**: JwtFilter에서 모든 요청에 대한 토큰 검증
- **권한 확인**: 사용자 role 기반 접근 제어
- **감사 로깅**: UserInfoExtractor에서 현재 사용자 정보 추출
- **토큰 갱신**: Refresh Token 기반 Access Token 재발급

## 9. 예외 처리
- **ExpiredJwtException**: 토큰 만료 시 발생, isExpired()에서 처리
- **MalformedJwtException**: 잘못된 토큰 형식
- **SignatureException**: 서명 검증 실패
- **IllegalArgumentException**: 잘못된 인자
