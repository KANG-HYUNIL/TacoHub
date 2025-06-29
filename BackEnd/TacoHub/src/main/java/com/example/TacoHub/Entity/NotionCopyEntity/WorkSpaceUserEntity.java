package com.example.TacoHub.Entity.NotionCopyEntity;


import com.example.TacoHub.Entity.AccountEntity;
import com.example.TacoHub.Entity.BaseDateEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "workspace_user") // Uncomment if you want to specify a table name
public class WorkSpaceUserEntity extends BaseDateEntity {


    @Id
    @GeneratedValue(generator = "UUID", strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id; // Workspace User ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private WorkSpaceEntity workspace; // Workspace to which this user belongs

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AccountEntity user; // User associated with this workspace




}
