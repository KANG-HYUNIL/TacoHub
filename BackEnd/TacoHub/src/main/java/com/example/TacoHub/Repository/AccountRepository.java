package com.example.TacoHub.Repository;

import com.example.TacoHub.Entity.AccountEntity;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사용자 계정 정보에 대한 데이터 액세스를 제공하는 리포지토리 인터페이스
 */
public interface AccountRepository extends JpaRepository<AccountEntity, String> {

    /**
     * 주어진 이메일 ID로 사용자 존재 여부를 확인한다.
     * 
     * @param emailId 확인할 이메일 ID
     * @return 이메일 ID가 존재하면 true, 존재하지 않으면 false 반환
     */
    boolean existsByEmailId(String emailId);

    /**
     * 주어진 이메일 ID가 포함된 사용자를 찾는다.
     * 
     * @param emailId 검색할 이메일 ID (부분 일치)
     * @return 찾은 사용자 계정 정보를 Optional로 반환
     */
    @Nonnull
    Optional<AccountEntity> findByEmailIdContaining(@Nonnull String emailId);

    /**
     * 주어진 이름이 포함된 사용자를 찾는다.
     * 
     * @param name 검색할 사용자 이름 (부분 일치)
     * @return 찾은 사용자 계정 정보를 Optional로 반환
     */
    @Nonnull
    Optional<AccountEntity> findByNameContaining(@Nonnull String name);

    /**
     * 주어진 이메일 ID와 정확히 일치하는 사용자를 찾는다.
     * 
     * @param emailId 검색할 이메일 ID (완전 일치)
     * @return 찾은 사용자 계정 정보를 Optional로 반환
     */
    @Nonnull
    Optional<AccountEntity> findByEmailId(@Nonnull String emailId);

}
