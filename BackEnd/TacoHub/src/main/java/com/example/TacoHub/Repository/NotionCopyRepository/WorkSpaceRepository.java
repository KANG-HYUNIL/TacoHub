package com.example.TacoHub.Repository.NotionCopyRepository;

import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkSpaceRepository extends JpaRepository<WorkSpaceEntity, UUID> {
    
    // 이름으로 워크스페이스 조회 (중복 확인용)
    Optional<WorkSpaceEntity> findByName(String name);
    
    //Id로 workspace find
    Optional<WorkSpaceEntity> findById(UUID id);

    // 이름 존재 여부 확인 (더 효율적)
    boolean existsByName(String name);

    boolean existsById(UUID id); // id 로 존재 여부 확
    
    // 이름에 특정 문자열이 포함된 워크스페이스들 조회 (검색용)
    List<WorkSpaceEntity> findByNameContaining(String keyword);
    
    //Id로 workspace delete
    void deleteById(UUID id);

    // 특정 사용자가 속한 워크스페이스들 조회 (WorkSpaceUserEntity와 조인)
    // 이 메서드는 WorkSpaceUserRepository에서 처리하는 것이 더 적절할 수 있음
    
}
