package com.example.TacoHub.Entity.NotionCopyEntity;

import com.example.TacoHub.Entity.BaseDateEntity;
import com.example.TacoHub.Enum.NotionCopyEnum.InvitationStatus;
import com.example.TacoHub.Enum.NotionCopyEnum.WorkSpaceRole;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 초대 엔티티
 * 워크스페이스 초대 정보를 저장하는 엔티티
 * 이메일, 토큰, 상태 등의 정보를 포함
 */
@Entity
@Table(name = "invitations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationEntity extends BaseDateEntity {
    
    /**
     * 초대 토큰 (UUID)
     */
    @Id
    @Column(name = "invitation_token", nullable = false, unique = true)
    private UUID invitationToken;
    
    /**
     * 초대한 사람 이메일
     */
    @Column(name = "invited_by", nullable = false)
    private String invitedBy;
    
    /**
     * 초대받은 사람 이메일
     */
    @Column(name = "invited_email", nullable = false)
    private String invitedEmail;
    
    /**
     * 워크스페이스 ID
     */
    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;
    
    /**
     * 초대된 역할
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private WorkSpaceRole role;
    
    /**
     * 초대 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvitationStatus status;
    
    /**
     * 초대 만료 시간
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    /**
     * 초대 수락 시간
     */
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    
    /**
     * 초대 메시지 (선택사항)
     */
    @Column(name = "custom_message", length = 500)
    private String customMessage;
}
