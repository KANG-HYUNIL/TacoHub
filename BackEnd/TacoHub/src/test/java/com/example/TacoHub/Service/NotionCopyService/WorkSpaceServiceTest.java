package com.example.TacoHub.Service.NotionCopyService;

import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.TacoHub.Entity.NotionCopyEntity.PageEntity;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceEntity;
import com.example.TacoHub.Repository.NotionCopyRepository.WorkSpaceRepository;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceNotFoundException;
import com.example.TacoHub.Repository.NotionCopyRepository.WorkSpaceRepository;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkSpaceService 테스트")
public class WorkSpaceServiceTest {
    
    @Mock
    private WorkSpaceRepository workspaceRepository;
    
    @Mock
    private PageService pageService;
    
    @InjectMocks
    private WorkSpaceService workSpaceService;

    @Test
    @DisplayName("워크스페이스 생성 성공")
    void createWorkspaceEntity_Success() {
        // Given
        String workspaceName = "Test Workspace"; // 예시 워크스페이스 이름
        UUID workspaceId = UUID.randomUUID(); // 예시 워크스페이스 ID
        
        // Mocking the behavior of workspaceRepository and pageService
        WorkSpaceEntity mockWorkspace = WorkSpaceEntity.builder()
                .id(workspaceId)
                .name(workspaceName)
                .build();
        
        PageEntity mockPage = PageEntity.builder()
                .id(UUID.randomUUID())
                .title("Default Page")
                .workspace(mockWorkspace)
                .isRoot(true)
                .build();

        // Mocking the repository save method to return the mock workspace
        when(workspaceRepository.save(any(WorkSpaceEntity.class))).thenReturn(mockWorkspace);
        when(pageService.createPageEntity(eq(workspaceId), isNull())).thenReturn(mockPage);

        // When
        WorkSpaceEntity result = workSpaceService.createWorkspaceEntity(workspaceName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(workspaceId);
        assertThat(result.getName()).isEqualTo(workspaceName);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        
        // Verify interactions
        verify(workspaceRepository).save(any(WorkSpaceEntity.class));
        verify(pageService).createPageEntity(eq(workspaceId), isNull());
    }

    @Test
    @DisplayName("워크스페이스 이름 변경 성공 테스트")
    void editWorkspaceName_ValidInput_UpdatesSuccessfully() {
        // Given
        UUID workspaceId = UUID.randomUUID();
        String currentName = "Old Workspace"; // 예시 현재 워크스페이스 이름
        String newName = "New Workspace"; // 예시 새로운 워크스페이스 이름
        
        // Mocking the existing workspace and the updated workspace
        WorkSpaceEntity existingWorkspace = WorkSpaceEntity.builder()
                .id(workspaceId)
                .name(currentName)
                .build();
        
        WorkSpaceEntity updatedWorkspace = WorkSpaceEntity.builder()
                .id(workspaceId)
                .name(newName)
                .build();
        
        // Mocking the repository behavior
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(existingWorkspace));
        when(workspaceRepository.save(any(WorkSpaceEntity.class))).thenReturn(updatedWorkspace);
        
        // When
        workSpaceService.editWorkspaceName(newName, workspaceId);
        
        // Then
        verify(workspaceRepository).findById(workspaceId);
        verify(workspaceRepository).save(any(WorkSpaceEntity.class));
    }

    @Test
    @DisplayName("워크스페이스 이름 변경 실패 - 존재하지 않는 ID")
    void editWorkspaceName_NonExistentId_ThrowsException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        String newName = "New Workspace";
        
        // Mocking the repository to return empty for non-existent ID
        when(workspaceRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> workSpaceService.editWorkspaceName(newName, nonExistentId))
                .isInstanceOf(WorkSpaceNotFoundException.class)
                .hasMessageContaining("WorkSpace not found");
        
        verify(workspaceRepository).findById(nonExistentId);
        verify(workspaceRepository, never()).save(any(WorkSpaceEntity.class));
    }

    @Test
    @DisplayName("워크스페이스 삭제 성공 테스트")
    void deleteWorkspace_ValidId_DeletesSuccessfully() {
        // Given
        UUID workspaceId = UUID.randomUUID();
        
        // Mocking the existing workspace and the delete behavior
        WorkSpaceEntity existingWorkspace = WorkSpaceEntity.builder()
                .id(workspaceId)
                .name("Test Workspace")
                .build();
        
        // Mocking the repository to return the existing workspace
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(existingWorkspace));
        doNothing().when(workspaceRepository).deleteById(workspaceId);
        
        // When
        workSpaceService.deleteWorkspace(workspaceId);
        
        // Then
        verify(workspaceRepository).findById(workspaceId);
        verify(workspaceRepository).deleteById(workspaceId);
    }

    @Test
    @DisplayName("워크스페이스 삭제 실패 - 존재하지 않는 ID")
    void deleteWorkspace_NonExistentId_ThrowsException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        
        // Mocking the repository to return empty for non-existent ID
        when(workspaceRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> workSpaceService.deleteWorkspace(nonExistentId))
                .isInstanceOf(WorkSpaceNotFoundException.class)
                .hasMessageContaining("WorkSpace not found");
        
        verify(workspaceRepository).findById(nonExistentId);
        verify(workspaceRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("워크스페이스 조회 성공 테스트")
    void getWorkspaceDto_ValidId_ReturnsWorkspaceDto() {
        // TODO: 구현 예정
    }

    @Test
    @DisplayName("워크스페이스 조회 실패 - 존재하지 않는 ID")
    void getWorkspaceDto_NonExistentId_ThrowsException() {
        // TODO: 구현 예정
    }

    @Test
    @DisplayName("워크스페이스 엔티티 조회 성공 테스트")
    void getWorkSpaceEntityOrThrow_ValidId_ReturnsEntity() {
        // TODO: 구현 예정 (private 메서드이므로 간접 테스트)
    }
}
