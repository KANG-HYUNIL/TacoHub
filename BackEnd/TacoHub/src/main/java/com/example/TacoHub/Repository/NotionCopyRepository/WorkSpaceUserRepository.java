package com.example.TacoHub.Repository.NotionCopyRepository;

import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceUserEntity;
import com.example.TacoHub.Enum.NotionCopyEnum.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkSpaceUserRepository extends JpaRepository<WorkSpaceUserEntity, UUID> {

    // 특정 사용자와 워크스페이스 관계 조회
    Optional<WorkSpaceUserEntity> findByUser_EmailIdAndWorkspace_Id(String emailId, UUID workspaceId);
    
    // 특정 사용자와 워크스페이스 관계 존재 여부
    boolean existsByUser_EmailIdAndWorkspace_Id(String emailId, UUID workspaceId);
    
    // 특정 워크스페이스의 모든 활성 사용자 조회
    List<WorkSpaceUserEntity> findByWorkspace_IdAndMembershipStatus(UUID workspaceId, MembershipStatus status);
    
    // 특정 사용자가 속한 모든 워크스페이스 조회 (멤버십 상태별)
    List<WorkSpaceUserEntity> findByUser_EmailIdAndMembershipStatus(String emailId, MembershipStatus status);

    
    // 특정 workspace의 모든 row 삭제
    @Modifying
    @Transactional
    void deleteByWorkspace_Id(UUID workspaceId);

}
