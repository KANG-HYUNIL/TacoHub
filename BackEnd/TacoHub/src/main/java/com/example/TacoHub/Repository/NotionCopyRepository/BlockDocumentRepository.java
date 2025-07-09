package com.example.TacoHub.Repository.NotionCopyRepository;

import com.example.TacoHub.Document.BlockDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlockDocumentRepository extends MongoRepository<BlockDocument, UUID> {
    
    /**
     * 페이지 ID와 부모 ID, 삭제 여부로 블록 목록 조회
     * @param pageId 페이지 ID
     * @param parentId 부모 블록 ID (null 가능)
     * @param isDeleted 삭제 여부
     * @return 조건에 맞는 블록 목록
     */
    List<BlockDocument> findByPageIdAndParentIdAndIsDeleted(UUID pageId, UUID parentId, Boolean isDeleted);
    
    /**
     * 페이지 ID로 삭제되지 않은 블록들을 순서대로 조회
     * @param pageId 페이지 ID
     * @param isDeleted 삭제 여부
     * @return 순서대로 정렬된 블록 목록
     */
    List<BlockDocument> findByPageIdAndIsDeletedOrderByOrderIndex(UUID pageId, Boolean isDeleted);
    
    /**
     * 부모 블록 ID로 자식 블록들 조회
     * @param parentId 부모 블록 ID
     * @param isDeleted 삭제 여부
     * @return 자식 블록 목록
     */
    List<BlockDocument> findByParentIdAndIsDeletedOrderByOrderIndex(UUID parentId, Boolean isDeleted);
    
    /**
     * ID로 삭제되지 않은 블록 조회
     * @param id 블록 ID
     * @return 블록 (삭제되지 않은 것만)
     */
    Optional<BlockDocument> findByIdAndIsDeleted(UUID id, Boolean isDeleted);


    // PageId로 모든 block 삭제
    void deleteByPageId(UUID pageId);
}
