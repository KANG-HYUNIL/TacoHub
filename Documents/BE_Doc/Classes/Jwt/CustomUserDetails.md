# CustomUserDetails

**패키지:** com.example.TacoHub.Utils.Jwt

## 1. 개요
`CustomUserDetails`는 Spring Security의 `UserDetails` 인터페이스를 구현하여 사용자 인증 정보를 제공하는 클래스입니다. AccountEntity 기반으로 Spring Security의 인증 과정에서 필요한 사용자 정보와 권한을 처리합니다.

## 2. 의존성 및 환경
- **Spring Security**: `UserDetails`, `GrantedAuthority`, `SimpleGrantedAuthority`
- **TacoHub Entity**: `AccountEntity` (사용자 계정 정보)
- **JDK Collections**: `Collections`, `Collection`

## 3. 클래스 멤버 및 의미
### 3.1 `accountEntity` (AccountEntity, final)
- **의미**: 실제 사용자 계정 정보를 담고 있는 엔티티 객체
- **역할**: Spring Security 인증에 필요한 모든 사용자 정보의 원본 소스
- **접근성**: private final로 불변성 보장

## 4. 생성자
### 4.1 `CustomUserDetails(AccountEntity accountEntity)`
- **역할**: AccountEntity를 받아 CustomUserDetails 객체를 초기화
- **인자**: 
  - `accountEntity` (AccountEntity): 사용자 계정 엔티티
- **동작**: 전달받은 AccountEntity를 멤버 변수에 저장
- **예외**: accountEntity가 null인 경우 NullPointerException 발생 가능

## 5. 메서드 상세 설명

### 5.1 `Collection<? extends GrantedAuthority> getAuthorities()`
- **역할**: 사용자가 가진 권한 목록을 Spring Security 형식으로 반환
- **인자**: 없음
- **반환값**: `Collection<? extends GrantedAuthority>` - 사용자 권한 컬렉션
- **동작**:
  1. AccountEntity에서 role 필드 추출
  2. SimpleGrantedAuthority 객체로 래핑
  3. Collections.singletonList로 단일 권한 리스트 반환
- **예외**: 없음 (role이 null이어도 SimpleGrantedAuthority가 처리)

### 5.2 `String getPassword()`
- **역할**: 사용자의 암호화된 비밀번호 반환
- **인자**: 없음
- **반환값**: `String` - 암호화된 비밀번호
- **동작**: AccountEntity의 password 필드를 그대로 반환
- **예외**: AccountEntity의 password가 null인 경우 null 반환

### 5.3 `String getUsername()`
- **역할**: 사용자의 식별자(사용자명) 반환
- **인자**: 없음
- **반환값**: `String` - 사용자 식별자 (이메일 ID)
- **동작**: AccountEntity의 emailId 필드를 사용자명으로 반환
- **특이사항**: TacoHub에서는 이메일이 사용자 식별자 역할

### 5.4 `boolean isAccountNonExpired()`
- **역할**: 계정 만료 여부 확인
- **인자**: 없음
- **반환값**: `boolean` - 계정이 만료되지 않았으면 true
- **동작**: 항상 true 반환 (현재 구현에서는 계정 만료 기능 미사용)
- **확장 가능성**: 추후 AccountEntity에 만료일 필드 추가 시 로직 변경 필요

### 5.5 `boolean isAccountNonLocked()`
- **역할**: 계정 잠금 여부 확인
- **인자**: 없음
- **반환값**: `boolean` - 계정이 잠기지 않았으면 true
- **동작**: 항상 true 반환 (현재 구현에서는 계정 잠금 기능 미사용)
- **확장 가능성**: 추후 계정 잠금 기능 추가 시 로직 변경 필요

### 5.6 `boolean isCredentialsNonExpired()`
- **역할**: 인증 정보(비밀번호) 만료 여부 확인
- **인자**: 없음
- **반환값**: `boolean` - 인증 정보가 만료되지 않았으면 true
- **동작**: 항상 true 반환 (현재 구현에서는 비밀번호 만료 기능 미사용)
- **확장 가능성**: 추후 비밀번호 만료 정책 추가 시 로직 변경 필요

### 5.7 `boolean isEnabled()`
- **역할**: 계정 활성화 상태 확인
- **인자**: 없음
- **반환값**: `boolean` - 계정이 활성화되어 있으면 true
- **동작**: 항상 true 반환 (현재 구현에서는 모든 계정이 활성 상태)
- **확장 가능성**: 추후 계정 비활성화 기능 추가 시 로직 변경 필요

## 6. 동작 흐름
1. CustomUserDetailsService에서 DB 조회를 통해 AccountEntity 획득
2. AccountEntity를 인자로 CustomUserDetails 객체 생성
3. Spring Security 인증 과정에서 각 메서드 호출을 통해 사용자 정보 검증
4. JWT 토큰 생성 시 사용자 정보 활용

## 7. 활용 시나리오
- **로그인 인증**: CustomUserDetailsService와 연동하여 사용자 인증
- **JWT 토큰 생성**: 인증 성공 시 사용자 정보를 JWT에 포함
- **권한 검사**: Spring Security의 권한 기반 접근 제어
- **감사 로깅**: UserInfoExtractor에서 현재 사용자 정보 추출

## 8. 보안 고려사항
- **비밀번호 노출 방지**: getPassword()는 암호화된 값만 반환
- **권한 일관성**: role 필드가 Spring Security 권한 체계와 일치해야 함
- **계정 상태 관리**: 추후 계정 상태 관리 기능 확장 시 보안 정책 반영 필요

## 9. 확장 가능성
- 계정 만료, 잠금, 비활성화 기능 추가
- 다중 권한 지원 (현재는 단일 권한만 지원)
- 추가 사용자 정보 필드 지원
