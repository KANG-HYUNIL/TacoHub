package com.example.TacoHub.Entity.NotionCopyEntity;

import com.example.TacoHub.Entity.BaseDateEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@Table(name = "page") // Specify the table name if needed
public class PageEntity extends BaseDateEntity {

    @Id
    @GeneratedValue(generator = "UUID", strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id; // Page ID

    @Column(name = "title")
    private String title; // Page Title

    @Column(name = "path")
    private String path; // Page Path

    @Column(name = "block_id")
    private String blockId; // Block ID associated with the page

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private WorkSpaceEntity workspace; // Workspace to which this page belongs

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_page_id")
    private PageEntity parentPage; // Parent page if this is a sub-page

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentPage", cascade = CascadeType.ALL)
    @OrderBy("orderIndex ASC")
    private List<PageEntity> childPages;

    @Column(name = "order_index")
    private Integer orderIndex; // Order index for sorting child pages

    @Column(name="is_root", nullable = false)
    private Boolean isRoot; // Indicates if this page is a root page (not a sub-page)

}

