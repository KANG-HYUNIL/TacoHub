package com.example.TacoHub.Entity.NotionCopyEntity;

import com.example.TacoHub.Entity.BaseDateEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.service.annotation.GetExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@Table(name = "workspace") // Specify the table name if needed
public class WorkSpaceEntity extends BaseDateEntity {
    @Id
    @GeneratedValue(generator = "UUID", strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id; // Workspace ID

    @Column(name = "name", nullable = false)
    private String name; // Workspace Name

    @OneToMany(mappedBy = "workspace", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PageEntity> rootPages;

}
