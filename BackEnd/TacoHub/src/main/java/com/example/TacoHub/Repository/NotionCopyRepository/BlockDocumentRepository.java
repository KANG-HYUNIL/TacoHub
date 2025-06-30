package com.example.TacoHub.Repository.NotionCopyRepository;

import com.example.TacoHub.Document.BlockDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BlockDocumentRepository extends MongoRepository<BlockDocument, UUID> {
}
