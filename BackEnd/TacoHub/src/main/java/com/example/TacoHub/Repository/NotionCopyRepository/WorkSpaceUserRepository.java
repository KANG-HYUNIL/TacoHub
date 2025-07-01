package com.example.TacoHub.Repository.NotionCopyRepository;

import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkSpaceUserRepository extends JpaRepository<WorkSpaceUserEntity, UUID> {

    // 특정 사용자와 워크스페이스 관계 조회
    Optional<WorkSpaceUserEntity> findByUser_EmailIdAndWorkspace_Id(String emailId, UUID workspaceId);
    
    // 특정 사용자와 워크스페이스 관계 존재 여부
    boolean existsByUser_EmailIdAndWorkspace_Id(String emailId, UUID workspaceId);
    
    // 특정 워크스페이스의 모든 활성 사용자 조회 (추후 필요시)
    // List<WorkSpaceUserEntity> findByWorkspace_IdAndMembershipStatus(UUID workspaceId, MembershipStatus status);

}
