package com.example.TacoHub.Service.NotionCopyService;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.TacoHub.Entity.NotionCopyEntity.PageEntity;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceEntity;
import com.example.TacoHub.Exception.NotionCopyException.PageNotFoundException;
import com.example.TacoHub.Exception.NotionCopyException.PageOperationException;
import com.example.TacoHub.Repository.NotionCopyRepository.PageRepository;
import com.example.TacoHub.Repository.NotionCopyRepository.WorkSpaceRepository;

/**
 * PageService 단위 테스트
 * Given/When/Then 패턴과 현업 수준의 테스트 케이스 작성
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PageService 단위 테스트")
public class PageServiceTest {
    
    @Mock
    private PageRepository pageRepository;
    
    @Mock
    private BlockService blockService;
    
    @Mock
    private WorkSpaceRepository workspaceRepository;
    
    @InjectMocks
    private PageService pageService;
    
    // 테스트용 상수 정의
    private static final UUID VALID_WORKSPACE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID VALID_PAGE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final UUID VALID_PARENT_PAGE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    private static final String VALID_PAGE_TITLE = "테스트 페이지";
    private static final String VALID_WORKSPACE_NAME = "테스트 워크스페이스";
    
    private PageEntity validPageEntity;
    private WorkSpaceEntity validWorkSpaceEntity;
    private PageEntity validParentPageEntity;
    
    @BeforeEach
    void setUp() {
        // Given: 테스트용 유효한 WorkSpaceEntity 준비
        validWorkSpaceEntity = WorkSpaceEntity.builder()
                .id(VALID_WORKSPACE_ID)
                .name(VALID_WORKSPACE_NAME)
                .build();
        
        // Given: 테스트용 유효한 PageEntity 준비
        validPageEntity = PageEntity.builder()
                .id(VALID_PAGE_ID)
                .title(VALID_PAGE_TITLE)
                .workspace(validWorkSpaceEntity)
                .build();
        
        // Given: 테스트용 유효한 부모 PageEntity 준비
        validParentPageEntity = PageEntity.builder()
                .id(VALID_PARENT_PAGE_ID)
                .title("부모 페이지")
                .workspace(validWorkSpaceEntity)
                .build();
    }
    
    @Nested
    @DisplayName("createPageEntity (워크스페이스 루트) 메서드 테스트")
    class CreatePageEntityRootTest {
        
        @Test
        @DisplayName("유효한 워크스페이스 ID가 주어졌을 때 루트 페이지 생성이 성공해야 한다")
        void createPageEntity_WithValidWorkspaceId_ShouldSucceed() {
            // Given: 유효한 워크스페이스 ID와 워크스페이스가 존재하는 상황
            given(workspaceRepository.findById(VALID_WORKSPACE_ID)).willReturn(Optional.of(validWorkSpaceEntity));
            given(pageRepository.save(any(PageEntity.class))).willReturn(validPageEntity);
            
            // When: 루트 페이지 생성 실행
            PageEntity result = pageService.createPageEntity(VALID_WORKSPACE_ID);
            
            // Then: 생성된 페이지가 반환되어야 함
            assertThat(result).isNotNull();
            assertThat(result.getWorkspace()).isEqualTo(validWorkSpaceEntity);
            
            // Then: 외부 의존성이 정확히 호출되어야 함
            then(workspaceRepository).should(times(1)).findById(VALID_WORKSPACE_ID);
            then(pageRepository).should(times(1)).save(any(PageEntity.class));
        }
        
        @Test
        @DisplayName("null 워크스페이스 ID가 주어졌을 때 PageOperationException이 발생해야 한다")
        void createPageEntity_WithNullWorkspaceId_ShouldThrowPageOperationException() {
            // Given: null 워크스페이스 ID
            UUID nullWorkspaceId = null;
            
            // When & Then: PageOperationException이 발생해야 함
            assertThatThrownBy(() -> pageService.createPageEntity(nullWorkspaceId))
                    .isInstanceOf(PageOperationException.class)
                    .hasMessageContaining("워크스페이스 ID는 필수입니다");
            
            // Then: 외부 의존성은 호출되지 않아야 함
            then(workspaceRepository).should(never()).findById(any());
            then(pageRepository).should(never()).save(any(PageEntity.class));
        }
        
        @Test
        @DisplayName("존재하지 않는 워크스페이스 ID가 주어졌을 때 예외가 발생해야 한다")
        void createPageEntity_WithNonExistentWorkspace_ShouldThrowException() {
            // Given: 존재하지 않는 워크스페이스 ID
            given(workspaceRepository.findById(VALID_WORKSPACE_ID)).willReturn(Optional.empty());
            
            // When & Then: 예외가 발생해야 함
            assertThatThrownBy(() -> pageService.createPageEntity(VALID_WORKSPACE_ID))
                    .isInstanceOf(RuntimeException.class);
            
            // Then: 워크스페이스 조회는 되지만 페이지 저장은 되지 않아야 함
            then(workspaceRepository).should(times(1)).findById(VALID_WORKSPACE_ID);
            then(pageRepository).should(never()).save(any(PageEntity.class));
        }
    }
    
    @Nested
    @DisplayName("createPageEntity (하위 페이지) 메서드 테스트")
    class CreatePageEntityChildTest {
        
        @Test
        @DisplayName("유효한 워크스페이스 ID와 부모 페이지 ID가 주어졌을 때 하위 페이지 생성이 성공해야 한다")
        void createPageEntity_WithValidIds_ShouldSucceed() {
            // Given: 유효한 워크스페이스 ID, 부모 페이지 ID와 모든 엔티티가 존재하는 상황
            given(workspaceRepository.findById(VALID_WORKSPACE_ID)).willReturn(Optional.of(validWorkSpaceEntity));
            given(pageRepository.findById(VALID_PARENT_PAGE_ID)).willReturn(Optional.of(validParentPageEntity));
            given(pageRepository.save(any(PageEntity.class))).willReturn(validPageEntity);
            
            // When: 하위 페이지 생성 실행
            PageEntity result = pageService.createPageEntity(VALID_WORKSPACE_ID, VALID_PARENT_PAGE_ID);
            
            // Then: 생성된 페이지가 반환되어야 함
            assertThat(result).isNotNull();
            assertThat(result.getWorkspace()).isEqualTo(validWorkSpaceEntity);
            
            // Then: 모든 외부 의존성이 정확히 호출되어야 함
            then(workspaceRepository).should(times(1)).findById(VALID_WORKSPACE_ID);
            then(pageRepository).should(times(1)).findById(VALID_PARENT_PAGE_ID);
            then(pageRepository).should(times(1)).save(any(PageEntity.class));
        }
        
        @Test
        @DisplayName("null 부모 페이지 ID가 주어졌을 때 PageOperationException이 발생해야 한다")
        void createPageEntity_WithNullParentPageId_ShouldThrowPageOperationException() {
            // Given: null 부모 페이지 ID
            UUID nullParentPageId = null;
            
            // When & Then: PageOperationException이 발생해야 함
            assertThatThrownBy(() -> pageService.createPageEntity(VALID_WORKSPACE_ID, nullParentPageId))
                    .isInstanceOf(PageOperationException.class)
                    .hasMessageContaining("부모 페이지 ID는 필수입니다");
            
            // Then: 외부 의존성은 호출되지 않아야 함
            then(workspaceRepository).should(never()).findById(any());
            then(pageRepository).should(never()).findById(any());
            then(pageRepository).should(never()).save(any(PageEntity.class));
        }
        
        @Test
        @DisplayName("존재하지 않는 부모 페이지 ID가 주어졌을 때 PageNotFoundException이 발생해야 한다")
        void createPageEntity_WithNonExistentParentPage_ShouldThrowPageNotFoundException() {
            // Given: 존재하지 않는 부모 페이지 ID
            given(workspaceRepository.findById(VALID_WORKSPACE_ID)).willReturn(Optional.of(validWorkSpaceEntity));
            given(pageRepository.findById(VALID_PARENT_PAGE_ID)).willReturn(Optional.empty());
            
            // When & Then: PageNotFoundException이 발생해야 함
            assertThatThrownBy(() -> pageService.createPageEntity(VALID_WORKSPACE_ID, VALID_PARENT_PAGE_ID))
                    .isInstanceOf(PageNotFoundException.class);
            
            // Then: 부모 페이지 조회까지만 호출되어야 함
            then(workspaceRepository).should(times(1)).findById(VALID_WORKSPACE_ID);
            then(pageRepository).should(times(1)).findById(VALID_PARENT_PAGE_ID);
            then(pageRepository).should(never()).save(any(PageEntity.class));
        }
    }
    
    @Nested
    @DisplayName("deletePageEntityByWorkspaceId 메서드 테스트")
    class DeletePageEntityByWorkspaceIdTest {
        
        @Test
        @DisplayName("유효한 워크스페이스 ID가 주어졌을 때 워크스페이스의 모든 페이지 삭제가 성공해야 한다")
        void deletePageEntityByWorkspaceId_WithValidId_ShouldSucceed() {
            // Given: 유효한 워크스페이스 ID와 워크스페이스가 존재하는 상황
            given(workspaceRepository.findById(VALID_WORKSPACE_ID)).willReturn(Optional.of(validWorkSpaceEntity));
            //doNothing().when(blockService).deleteAllBlocksInWorkspace(VALID_WORKSPACE_ID);
            doNothing().when(pageRepository).deleteByWorkspace_Id(VALID_WORKSPACE_ID);
            
            // When: 워크스페이스의 모든 페이지 삭제 실행
            assertThatCode(() -> pageService.deletePageEntityByWorkspaceId(VALID_WORKSPACE_ID))
                    .doesNotThrowAnyException();
            
            // Then: 모든 외부 의존성이 정확히 호출되어야 함
            then(workspaceRepository).should(times(1)).findById(VALID_WORKSPACE_ID);
            //then(blockService).should(times(1)).deleteAllBlocksInWorkspace(VALID_WORKSPACE_ID);
            then(pageRepository).should(times(1)).deleteByWorkspace_Id(VALID_WORKSPACE_ID);
        }
        
        @Test
        @DisplayName("null 워크스페이스 ID가 주어졌을 때 PageOperationException이 발생해야 한다")
        void deletePageEntityByWorkspaceId_WithNullId_ShouldThrowPageOperationException() {
            // Given: null 워크스페이스 ID
            UUID nullWorkspaceId = null;
            
            // When & Then: PageOperationException이 발생해야 함
            assertThatThrownBy(() -> pageService.deletePageEntityByWorkspaceId(nullWorkspaceId))
                    .isInstanceOf(PageOperationException.class)
                    .hasMessageContaining("워크스페이스 ID는 필수입니다");
            
            // Then: 외부 의존성은 호출되지 않아야 함
            then(workspaceRepository).should(never()).findById(any());
            //then(blockService).should(never()).deleteAllBlocksInWorkspace(any());
            then(pageRepository).should(never()).deleteByWorkspace_Id(any());
        }
        
        @Test
        @DisplayName("존재하지 않는 워크스페이스 ID가 주어졌을 때 예외가 발생해야 한다")
        void deletePageEntityByWorkspaceId_WithNonExistentWorkspace_ShouldThrowException() {
            // Given: 존재하지 않는 워크스페이스 ID
            given(workspaceRepository.findById(VALID_WORKSPACE_ID)).willReturn(Optional.empty());
            
            // When & Then: 예외가 발생해야 함
            assertThatThrownBy(() -> pageService.deletePageEntityByWorkspaceId(VALID_WORKSPACE_ID))
                    .isInstanceOf(RuntimeException.class);
            
            // Then: 워크스페이스 조회만 호출되어야 함
            then(workspaceRepository).should(times(1)).findById(VALID_WORKSPACE_ID);
            //then(blockService).should(never()).deleteAllBlocksInWorkspace(any());
            then(pageRepository).should(never()).deleteByWorkspace_Id(any());
        }
    }
    
    @Nested
    @DisplayName("getPageEntityOrThrow 메서드 테스트")
    class GetPageEntityOrThrowTest {
        
        @Test
        @DisplayName("존재하는 페이지 ID가 주어졌을 때 PageEntity를 반환해야 한다")
        void getPageEntityOrThrow_WithExistingId_ShouldReturnEntity() {
            // Given: 존재하는 페이지 ID
            given(pageRepository.findById(VALID_PAGE_ID)).willReturn(Optional.of(validPageEntity));
            
            // When: PageEntity 조회 실행
            PageEntity result = pageService.getPageEntityOrThrow(VALID_PAGE_ID);
            
            // Then: 올바른 PageEntity가 반환되어야 함
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(VALID_PAGE_ID);
            assertThat(result.getTitle()).isEqualTo(VALID_PAGE_TITLE);
            
            // Then: Repository 메서드가 정확히 한 번 호출되어야 함
            then(pageRepository).should(times(1)).findById(VALID_PAGE_ID);
        }
        
        @Test
        @DisplayName("존재하지 않는 페이지 ID가 주어졌을 때 PageNotFoundException이 발생해야 한다")
        void getPageEntityOrThrow_WithNonExistentId_ShouldThrowPageNotFoundException() {
            // Given: 존재하지 않는 페이지 ID
            given(pageRepository.findById(VALID_PAGE_ID)).willReturn(Optional.empty());
            
            // When & Then: PageNotFoundException이 발생해야 함
            assertThatThrownBy(() -> pageService.getPageEntityOrThrow(VALID_PAGE_ID))
                    .isInstanceOf(PageNotFoundException.class)
                    .hasMessageContaining("페이지를 찾을 수 없습니다");
            
            // Then: Repository 메서드가 정확히 한 번 호출되어야 함
            then(pageRepository).should(times(1)).findById(VALID_PAGE_ID);
        }
        
        @Test
        @DisplayName("null 페이지 ID가 주어졌을 때 PageOperationException이 발생해야 한다")
        void getPageEntityOrThrow_WithNullId_ShouldThrowPageOperationException() {
            // Given: null 페이지 ID
            UUID nullPageId = null;
            
            // When & Then: PageOperationException이 발생해야 함
            assertThatThrownBy(() -> pageService.getPageEntityOrThrow(nullPageId))
                    .isInstanceOf(PageOperationException.class)
                    .hasMessageContaining("페이지 ID는 필수입니다");
            
            // Then: Repository 메서드는 호출되지 않아야 함
            then(pageRepository).should(never()).findById(any());
        }
    }
}
