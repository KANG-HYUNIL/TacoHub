package com.example.TacoHub.Repository.NotionCopyRepository;

import com.example.TacoHub.Entity.NotionCopyEntity.PageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, UUID> {

    // id로 PageEntity를 조회하는 메소드
    Optional<PageEntity> findById(UUID id);

    // id로 PageEntity 존재 여부 확인
    boolean existsById(UUID id);

    // id로 page delete
    void deleteById(UUID id);

    // 워크스페이스의 루트 페이지들을 orderIndex 순으로 조회
    List<PageEntity> findByWorkspace_IdAndParentPageIsNullOrderByOrderIndexAsc(UUID workspaceId);

    // 부모 페이지의 자식 페이지들을 orderIndex 순으로 조회
    List<PageEntity> findByParentPage_IdOrderByOrderIndexAsc(UUID parentPageId);

    // 워크스페이스의 모든 페이지 조회
    List<PageEntity> findAllByWorkspace_Id(UUID workspaceId);

    // 경로와 워크스페이스로 페이지 존재 확인
    boolean existsByPathAndWorkspace_Id(String path, UUID workspaceId);

    //Workspace Id로 PageEntity 삭제
    @Modifying
    @Transactional
    void deleteByWorkspace_Id(UUID workspaceId);
}
