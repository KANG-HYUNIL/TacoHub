package com.example.TacoHub.Document;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

@Document(collection = "Block" )
@Data
public class BlockDocument {

    @Id
    @GeneratedValue(generator = "UUID", strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id; // block User ID

    @Field("page_id")
    private UUID pageId; // Page ID to which this block belongs

    @Field("content")
    private String content; // Content of the block


}

