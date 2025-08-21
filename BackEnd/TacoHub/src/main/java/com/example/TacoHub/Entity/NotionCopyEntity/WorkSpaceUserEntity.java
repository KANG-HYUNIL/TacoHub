package com.example.TacoHub.Entity.NotionCopyEntity;


import com.example.TacoHub.Entity.AccountEntity;
import com.example.TacoHub.Entity.BaseDateEntity;
import com.example.TacoHub.Enum.NotionCopyEnum.MembershipStatus;
import com.example.TacoHub.Enum.NotionCopyEnum.WorkSpaceRole;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@Table(name = "workspace_user") // Uncomment if you want to specify a table name
public class WorkSpaceUserEntity extends BaseDateEntity {


    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id; // Workspace User ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private WorkSpaceEntity workspace; // Workspace to which this user belongs

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AccountEntity user; // User associated with this workspace

    @Enumerated(EnumType.STRING)
    @Column(name="workspace_role", nullable = false)
    private WorkSpaceRole workspaceRole; // Role of the user in the workspace (e.g., ADMIN, MEMBER)

    @Enumerated(EnumType.STRING)
    @Column(name= "membership_status", nullable = false)
    private MembershipStatus membershipStatus; // Status of the user's membership in the workspace (e.g., ACTIVE, INACTIVE)



}
