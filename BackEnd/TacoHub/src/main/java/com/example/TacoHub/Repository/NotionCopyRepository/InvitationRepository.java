package com.example.TacoHub.Repository.NotionCopyRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.TacoHub.Entity.NotionCopyEntity.InvitationEntity;
import com.example.TacoHub.Enum.NotionCopyEnum.InvitationStatus;

public interface InvitationRepository extends JpaRepository<InvitationEntity, UUID> {

    /**
     * 초대 토큰으로 InvitationEntity 조회
     * @param invitationToken 초대 토큰
     * @return InvitationEntity Optional
     */
    Optional<InvitationEntity> findByInvitationToken(UUID invitationToken);

    /**
     * 초대 토큰으로 InvitationEntity 존재 여부 확인
     * @param invitationToken 초대 토큰
     * @return 존재 여부
     */
    boolean existsByInvitationToken(UUID invitationToken);

    /**
     * 워크스페이스의 특정 상태 초대 목록 조회
     * @param workspaceId 워크스페이스 ID
     * @param status 초대 상태
     * @return 초대 목록
     */
    Optional<List<InvitationEntity>> findByWorkspaceIdAndStatus(UUID workspaceId, InvitationStatus status);

    /**
     * 이메일과 워크스페이스로 대기 중인 초대 조회
     * @param email 이메일
     * @param workspaceId 워크스페이스 ID
     * @param status 초대 상태
     * @return 초대 엔티티 Optional
     */
    Optional<InvitationEntity> findByInvitedEmailAndWorkspaceIdAndStatus(
        String email, UUID workspaceId, InvitationStatus status);

    /**
     * 이메일과 워크스페이스로 대기 중인 초대 존재 여부 확인
     * @param email 이메일
     * @param workspaceId 워크스페이스 ID
     * @param status 초대 상태
     * @return 존재 여부
     */
    boolean existsByInvitedEmailAndWorkspaceIdAndStatus(
        String email, UUID workspaceId, InvitationStatus status);


    /**
     * 만료된 초대들을 조회
     * @return 만료된 초대 목록
     */
    @Query("SELECT i FROM InvitationEntity i WHERE i.expiresAt < CURRENT_TIMESTAMP AND i.status = 'PENDING'")
    List<InvitationEntity> findExpiredInvitations();
}
