# AccountRepository

**경로:** `com.example.TacoHub.Repository.AccountRepository`

## 개요

사용자 계정 정보에 대한 데이터 액세스 계층을 담당하는 Repository 인터페이스입니다. JpaRepository를 상속받아 기본 CRUD 기능과 함께 계정 관련 특화된 쿼리 메서드를 제공합니다.

## 상속 관계

```java
public interface AccountRepository extends JpaRepository<AccountEntity, String>
```

- **제네릭 타입**: `<AccountEntity, String>`
  - Entity: AccountEntity
  - ID 타입: String (emailId가 기본 키)

## 주요 메서드

### 기본 CRUD 메서드 (JpaRepository 상속)

- `save(AccountEntity)`: 계정 정보 저장
- `findById(String)`: 이메일 ID로 계정 조회
- `findAll()`: 모든 계정 조회
- `delete(AccountEntity)`: 계정 삭제
- `count()`: 총 계정 수 조회

### 커스텀 쿼리 메서드

- **`existsByEmailId(String emailId)`**: 이메일 ID 존재 여부 확인
  - 반환 타입: boolean
  - 설명: 회원가입 시 이메일 중복 확인에 사용

- **`findByEmailIdContaining(String emailId)`**: 이메일 ID 부분 검색
  - 반환 타입: List<AccountEntity>
  - 설명: 관리자 기능에서 사용자 검색에 활용

- **`findByNameContaining(String name)`**: 이름 부분 검색
  - 반환 타입: List<AccountEntity>
  - 설명: 사용자 이름으로 검색 기능

- **`findByEmailId(String emailId)`**: 이메일 ID 정확 일치 조회
  - 반환 타입: Optional<AccountEntity>
  - 설명: 로그인, 프로필 조회 등에 사용

## 관련 클래스

- **Entity**: [AccountEntity](../Entity/AccountEntity.md)
- **DTO**: [AccountDto](../Dto/AccountDto.md)
- **Service**: [AccountService](../Service/AccountService.md)
- **Controller**: [AccountController](../Controller/AccountController.md)

## 사용 예시

### 1. 이메일 중복 확인
```java
@Service
public class AccountService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    public boolean isEmailExists(String emailId) {
        return accountRepository.existsByEmailId(emailId);
    }
}
```

### 2. 사용자 검색
```java
public List<AccountEntity> searchUsersByName(String searchTerm) {
    return accountRepository.findByNameContaining(searchTerm);
}
```

### 3. 로그인용 사용자 조회
```java
public Optional<AccountEntity> findUserForLogin(String emailId) {
    return accountRepository.findByEmailId(emailId);
}
```

## 쿼리 성능 최적화

### 데이터베이스 인덱스
```sql
-- 이메일 ID는 기본 키이므로 자동으로 인덱스 생성
-- 추가 인덱스 고려사항:
CREATE INDEX idx_account_name ON account(name);  -- 이름 검색용
```

### 쿼리 최적화 팁
- `existsByEmailId` vs `findByEmailId`: 존재 여부만 확인할 때는 exists 사용
- 부분 검색 시 와일드카드 위치 고려 (앞쪽 와일드카드는 인덱스 활용 불가)
- 대용량 데이터 처리 시 Pageable 인터페이스 활용 고려

## 트랜잭션 관리

- **읽기 전용 메서드**: `@Transactional(readOnly = true)` 적용 권장
- **쓰기 작업**: Service 계층에서 트랜잭션 관리
- **배치 작업**: `saveAll()` 메서드 활용으로 성능 향상
