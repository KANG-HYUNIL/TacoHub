package com.example.TacoHub.Repository.NotionCopyRepository;

import com.example.TacoHub.Entity.NotionCopyEntity.PageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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


}
