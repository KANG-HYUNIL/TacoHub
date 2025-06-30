package com.example.TacoHub.Repository.NotionCopyRepository;

import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkSpaceUserRepository extends JpaRepository<WorkSpaceUserEntity, UUID> {

}
