package com.example.TacoHub.Service.NotionCopyService;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.TacoHub.Dto.NotionCopyDTO.WorkSpaceDTO;
import com.example.TacoHub.Entity.NotionCopyEntity.PageEntity;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceEntity;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceNotFoundException;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceOperationException;
import com.example.TacoHub.Logging.UserInfoExtractor;
import com.example.TacoHub.Repository.NotionCopyRepository.WorkSpaceRepository;

/**
 * WorkSpaceService 단위 테스트
 * Given/When/Then 패턴과 현업 수준의 테스트 케이스 작성
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkSpaceService 단위 테스트")
public class WorkSpaceServiceTest {
    
    // ===== 테스트용 상수 정의 =====
    
    private static final UUID VALID_WORKSPACE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final String VALID_WORKSPACE_NAME = "테스트 워크스페이스";
    private static final String VALID_USER_EMAIL = "test@example.com";
    private static final String LONG_WORKSPACE_NAME = "a".repeat(101); // 100자 초과
    
    // ===== Mock 객체 =====
    
    @Mock
    private WorkSpaceRepository workspaceRepository;
    
    @Mock
    private PageService pageService;
    
    @Mock
    private WorkSpaceUserService workSpaceUserService;
    
    @Mock
    private UserInfoExtractor userInfoExtractor;
    
    @InjectMocks
    private WorkSpaceService workSpaceService;
    
    // ===== 테스트용 데이터 =====
    
    private WorkSpaceEntity validWorkSpaceEntity;
    private PageEntity validPageEntity;
    private WorkSpaceDTO validWorkSpaceDTO;
    
    @BeforeEach
    void setUp() {
        // Given: 테스트용 유효한 WorkSpaceEntity 준비
        validWorkSpaceEntity = WorkSpaceEntity.builder()
                .id(VALID_WORKSPACE_ID)
                .name(VALID_WORKSPACE_NAME)
                .rootPages(new ArrayList<>())
                .build();
        
        // Given: 테스트용 유효한 PageEntity 준비
        validPageEntity = PageEntity.builder()
                .id(UUID.randomUUID())
                .title("Test Page")
                .build();
        
        // Given: 테스트용 유효한 WorkSpaceDTO 준비
        validWorkSpaceDTO = WorkSpaceDTO.builder()
                .id(VALID_WORKSPACE_ID)
                .name(VALID_WORKSPACE_NAME)
                .build();
    }
    
    // ===== createWorkspaceEntity 테스트 =====
    
    @Test
    @DisplayName("유효한 워크스페이스 이름이 주어졌을 때 워크스페이스 생성이 성공해야 한다")
    void createWorkspaceEntity_WithValidName_ShouldSucceed() {
        // Given: 유효한 워크스페이스 이름과 현재 사용자 정보
        given(userInfoExtractor.getCurrentUserEmail()).willReturn(VALID_USER_EMAIL);
        given(workspaceRepository.save(any(WorkSpaceEntity.class))).willReturn(validWorkSpaceEntity);
        given(pageService.createPageEntity(any(UUID.class))).willReturn(validPageEntity);
        doNothing().when(workSpaceUserService).createAdminUserEntity(anyString(), any(UUID.class));
        
        // When: 워크스페이스 생성 실행
        WorkSpaceEntity result = workSpaceService.createWorkspaceEntity(VALID_WORKSPACE_NAME);
        
        // Then: 생성된 워크스페이스가 반환되어야 함
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(VALID_WORKSPACE_NAME);
        
        // Then: 외부 의존성이 정확히 호출되어야 함
        then(userInfoExtractor).should(times(1)).getCurrentUserEmail();
        then(workspaceRepository).should(times(2)).save(any(WorkSpaceEntity.class)); // 2번 저장
        then(pageService).should(times(1)).createPageEntity(any(UUID.class));
        then(workSpaceUserService).should(times(1)).createAdminUserEntity(VALID_USER_EMAIL, validWorkSpaceEntity.getId());
    }
    
    @Test
    @DisplayName("null 워크스페이스 이름이 주어졌을 때 WorkSpaceOperationException이 발생해야 한다")
    void createWorkspaceEntity_WithNullName_ShouldThrowWorkSpaceOperationException() {
        // Given: null 워크스페이스 이름
        String nullWorkspaceName = null;
        
        // When & Then: WorkSpaceOperationException이 발생해야 함
        assertThatThrownBy(() -> workSpaceService.createWorkspaceEntity(nullWorkspaceName))
                .isInstanceOf(WorkSpaceOperationException.class)
                .hasMessageContaining("워크스페이스 이름은 필수입니다");
        
        // Then: 외부 의존성은 호출되지 않아야 함
        then(userInfoExtractor).should(never()).getCurrentUserEmail();
        then(workspaceRepository).should(never()).save(any(WorkSpaceEntity.class));
    }
    
    @Test
    @DisplayName("빈 문자열 워크스페이스 이름이 주어졌을 때 WorkSpaceOperationException이 발생해야 한다")
    void createWorkspaceEntity_WithEmptyName_ShouldThrowWorkSpaceOperationException() {
        // Given: 빈 문자열 워크스페이스 이름
        String emptyWorkspaceName = "";
        
        // When & Then: WorkSpaceOperationException이 발생해야 함
        assertThatThrownBy(() -> workSpaceService.createWorkspaceEntity(emptyWorkspaceName))
                .isInstanceOf(WorkSpaceOperationException.class)
                .hasMessageContaining("워크스페이스 이름은 필수입니다");
        
        // Then: 외부 의존성은 호출되지 않아야 함
        then(userInfoExtractor).should(never()).getCurrentUserEmail();
        then(workspaceRepository).should(never()).save(any(WorkSpaceEntity.class));
    }
    
    @Test
    @DisplayName("100자를 초과하는 워크스페이스 이름이 주어졌을 때 WorkSpaceOperationException이 발생해야 한다")
    void createWorkspaceEntity_WithTooLongName_ShouldThrowWorkSpaceOperationException() {
        // Given: 100자를 초과하는 워크스페이스 이름
        
        // When & Then: WorkSpaceOperationException이 발생해야 함
        assertThatThrownBy(() -> workSpaceService.createWorkspaceEntity(LONG_WORKSPACE_NAME))
                .isInstanceOf(WorkSpaceOperationException.class)
                .hasMessageContaining("워크스페이스 이름은 100자를 초과할 수 없습니다");
        
        // Then: 외부 의존성은 호출되지 않아야 함
        then(userInfoExtractor).should(never()).getCurrentUserEmail();
        then(workspaceRepository).should(never()).save(any(WorkSpaceEntity.class));
    }
    
    @Test
    @DisplayName("현재 사용자 이메일을 확인할 수 없을 때 WorkSpaceOperationException이 발생해야 한다")
    void createWorkspaceEntity_WithNoCurrentUser_ShouldThrowWorkSpaceOperationException() {
        // Given: 현재 사용자 이메일이 null인 상황
        given(userInfoExtractor.getCurrentUserEmail()).willReturn(null);
        
        // When & Then: WorkSpaceOperationException이 발생해야 함
        assertThatThrownBy(() -> workSpaceService.createWorkspaceEntity(VALID_WORKSPACE_NAME))
                .isInstanceOf(WorkSpaceOperationException.class)
                .hasMessageContaining("현재 사용자 이메일을 확인할 수 없습니다");
        
        // Then: 사용자 정보는 조회되지만 저장은 되지 않아야 함
        then(userInfoExtractor).should(times(1)).getCurrentUserEmail();
        then(workspaceRepository).should(never()).save(any(WorkSpaceEntity.class));
    }
    
    // ===== editWorkspaceName 테스트 =====
    
    @Test
    @DisplayName("유효한 정보가 주어졌을 때 워크스페이스 이름 수정이 성공해야 한다")
    void editWorkspaceName_WithValidInput_ShouldSucceed() {
        // Given: 유효한 입력값
        String newName = "수정된 워크스페이스";
        given(workspaceRepository.findById(VALID_WORKSPACE_ID)).willReturn(Optional.of(validWorkSpaceEntity));
        given(workspaceRepository.save(any(WorkSpaceEntity.class))).willReturn(validWorkSpaceEntity);
        
        // When: 워크스페이스 이름 수정 실행
        assertThatCode(() -> workSpaceService.editWorkspaceName(newName, VALID_WORKSPACE_ID))
                .doesNotThrowAnyException();
        
        // Then: 모든 외부 의존성이 정확히 호출되어야 함
        then(workspaceRepository).should(times(1)).findById(VALID_WORKSPACE_ID);
        then(workspaceRepository).should(times(1)).save(any(WorkSpaceEntity.class));
    }
    
    @Test
    @DisplayName("null 워크스페이스 ID로 수정을 시도할 때 WorkSpaceOperationException이 발생해야 한다")
    void editWorkspaceName_WithNullWorkspaceId_ShouldThrowWorkSpaceOperationException() {
        // Given: null 워크스페이스 ID
        UUID nullWorkspaceId = null;
        
        // When & Then: WorkSpaceOperationException이 발생해야 함
        assertThatThrownBy(() -> workSpaceService.editWorkspaceName(VALID_WORKSPACE_NAME, nullWorkspaceId))
                .isInstanceOf(WorkSpaceOperationException.class)
                .hasMessageContaining("워크스페이스 ID는 필수입니다");
        
        // Then: 외부 의존성은 호출되지 않아야 함
        then(workspaceRepository).should(never()).findById(any());
    }
    
    @Test
    @DisplayName("존재하지 않는 워크스페이스 ID로 수정을 시도할 때 WorkSpaceNotFoundException이 발생해야 한다")
    void editWorkspaceName_WithNonExistentWorkspace_ShouldThrowWorkSpaceNotFoundException() {
        // Given: 존재하지 않는 워크스페이스 ID
        given(workspaceRepository.findById(VALID_WORKSPACE_ID)).willReturn(Optional.empty());
        
        // When & Then: WorkSpaceNotFoundException이 발생해야 함
        assertThatThrownBy(() -> workSpaceService.editWorkspaceName(VALID_WORKSPACE_NAME, VALID_WORKSPACE_ID))
                .isInstanceOf(WorkSpaceNotFoundException.class);
        
        // Then: 조회까지만 호출되어야 함
        then(workspaceRepository).should(times(1)).findById(VALID_WORKSPACE_ID);
        then(workspaceRepository).should(never()).save(any(WorkSpaceEntity.class));
    }
    
    // ===== deleteWorkspace 테스트 =====
    
    @Test
    @DisplayName("유효한 워크스페이스 ID가 주어졌을 때 워크스페이스 삭제가 성공해야 한다")
    void deleteWorkspace_WithValidId_ShouldSucceed() {
        // Given: 유효한 워크스페이스 ID와 권한이 있는 사용자
        given(workspaceRepository.existsById(VALID_WORKSPACE_ID)).willReturn(true);
        given(userInfoExtractor.getCurrentUserEmail()).willReturn(VALID_USER_EMAIL);
        given(workSpaceUserService.canUserManageWorkSpace(VALID_USER_EMAIL, VALID_WORKSPACE_ID)).willReturn(true);
        doNothing().when(pageService).deletePageEntityByWorkspaceId(VALID_WORKSPACE_ID);
        doNothing().when(workSpaceUserService).deleteWorkSpaceUserAllEntites(VALID_WORKSPACE_ID);
        doNothing().when(workspaceRepository).deleteById(VALID_WORKSPACE_ID);
        
        // When: 워크스페이스 삭제 실행
        assertThatCode(() -> workSpaceService.deleteWorkspace(VALID_WORKSPACE_ID))
                .doesNotThrowAnyException();
        
        // Then: 모든 외부 의존성이 정확히 호출되어야 함
        then(workspaceRepository).should(times(1)).existsById(VALID_WORKSPACE_ID);
        then(userInfoExtractor).should(times(1)).getCurrentUserEmail();
        then(workSpaceUserService).should(times(1)).canUserManageWorkSpace(VALID_USER_EMAIL, VALID_WORKSPACE_ID);
        then(pageService).should(times(1)).deletePageEntityByWorkspaceId(VALID_WORKSPACE_ID);
        then(workSpaceUserService).should(times(1)).deleteWorkSpaceUserAllEntites(VALID_WORKSPACE_ID);
        then(workspaceRepository).should(times(1)).deleteById(VALID_WORKSPACE_ID);
    }
    
    @Test
    @DisplayName("관리 권한이 없는 사용자가 삭제를 시도할 때 WorkSpaceOperationException이 발생해야 한다")
    void deleteWorkspace_WithoutPermission_ShouldThrowWorkSpaceOperationException() {
        // Given: 관리 권한이 없는 사용자
        given(workspaceRepository.existsById(VALID_WORKSPACE_ID)).willReturn(true);
        given(userInfoExtractor.getCurrentUserEmail()).willReturn(VALID_USER_EMAIL);
        given(workSpaceUserService.canUserManageWorkSpace(VALID_USER_EMAIL, VALID_WORKSPACE_ID)).willReturn(false);
        
        // When & Then: WorkSpaceOperationException이 발생해야 함
        assertThatThrownBy(() -> workSpaceService.deleteWorkspace(VALID_WORKSPACE_ID))
                .isInstanceOf(WorkSpaceOperationException.class)
                .hasMessageContaining("워크스페이스 관리 권한이 없습니다");
        
        // Then: 권한 확인까지만 호출되어야 함
        then(workspaceRepository).should(times(1)).existsById(VALID_WORKSPACE_ID);
        then(userInfoExtractor).should(times(1)).getCurrentUserEmail();
        then(workSpaceUserService).should(times(1)).canUserManageWorkSpace(VALID_USER_EMAIL, VALID_WORKSPACE_ID);
        then(pageService).should(never()).deletePageEntityByWorkspaceId(any(UUID.class));
        then(workspaceRepository).should(never()).deleteById(any(UUID.class));
    }
    
    // ===== getWorkspaceDto 테스트 =====
    
    @Test
    @DisplayName("존재하는 워크스페이스 ID가 주어졌을 때 WorkSpaceDTO를 반환해야 한다")
    void getWorkspaceDto_WithExistingId_ShouldReturnDto() {
        // Given: 존재하는 워크스페이스 ID
        given(workspaceRepository.findById(VALID_WORKSPACE_ID)).willReturn(Optional.of(validWorkSpaceEntity));
        
        // When: WorkSpaceDTO 조회 실행
        WorkSpaceDTO result = workSpaceService.getWorkspaceDto(VALID_WORKSPACE_ID);
        
        // Then: 올바른 WorkSpaceDTO가 반환되어야 함
        assertThat(result).isNotNull();
        
        // Then: Repository 메서드가 정확히 한 번 호출되어야 함
        then(workspaceRepository).should(times(1)).findById(VALID_WORKSPACE_ID);
    }
    
    @Test
    @DisplayName("존재하지 않는 워크스페이스 ID가 주어졌을 때 WorkSpaceNotFoundException이 발생해야 한다")
    void getWorkspaceDto_WithNonExistentId_ShouldThrowWorkSpaceNotFoundException() {
        // Given: 존재하지 않는 워크스페이스 ID
        given(workspaceRepository.findById(VALID_WORKSPACE_ID)).willReturn(Optional.empty());
        
        // When & Then: WorkSpaceNotFoundException이 발생해야 함
        assertThatThrownBy(() -> workSpaceService.getWorkspaceDto(VALID_WORKSPACE_ID))
                .isInstanceOf(WorkSpaceNotFoundException.class);
        
        // Then: Repository 메서드가 정확히 한 번 호출되어야 함
        then(workspaceRepository).should(times(1)).findById(VALID_WORKSPACE_ID);
    }
    
    @Test
    @DisplayName("null 워크스페이스 ID가 주어졌을 때 WorkSpaceOperationException이 발생해야 한다")
    void getWorkspaceDto_WithNullId_ShouldThrowWorkSpaceOperationException() {
        // Given: null 워크스페이스 ID
        UUID nullWorkspaceId = null;
        
        // When & Then: WorkSpaceOperationException이 발생해야 함
        assertThatThrownBy(() -> workSpaceService.getWorkspaceDto(nullWorkspaceId))
                .isInstanceOf(WorkSpaceOperationException.class)
                .hasMessageContaining("워크스페이스 ID는 필수입니다");
        
        // Then: Repository 메서드는 호출되지 않아야 함
        then(workspaceRepository).should(never()).findById(any());
    }
    
    // ===== getWorkSpaceEntityOrThrow 테스트 =====
    
    @Test
    @DisplayName("존재하는 워크스페이스 ID가 주어졌을 때 WorkSpaceEntity를 반환해야 한다")
    void getWorkSpaceEntityOrThrow_WithExistingId_ShouldReturnEntity() {
        // Given: 존재하는 워크스페이스 ID
        given(workspaceRepository.findById(VALID_WORKSPACE_ID)).willReturn(Optional.of(validWorkSpaceEntity));
        
        // When: WorkSpaceEntity 조회 실행
        WorkSpaceEntity result = workSpaceService.getWorkSpaceEntityOrThrow(VALID_WORKSPACE_ID);
        
        // Then: 올바른 WorkSpaceEntity가 반환되어야 함
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(VALID_WORKSPACE_ID);
        assertThat(result.getName()).isEqualTo(VALID_WORKSPACE_NAME);
        
        // Then: Repository 메서드가 정확히 한 번 호출되어야 함
        then(workspaceRepository).should(times(1)).findById(VALID_WORKSPACE_ID);
    }
    
    @Test
    @DisplayName("존재하지 않는 워크스페이스 ID가 주어졌을 때 WorkSpaceNotFoundException이 발생해야 한다")
    void getWorkSpaceEntityOrThrow_WithNonExistentId_ShouldThrowWorkSpaceNotFoundException() {
        // Given: 존재하지 않는 워크스페이스 ID
        given(workspaceRepository.findById(VALID_WORKSPACE_ID)).willReturn(Optional.empty());
        
        // When & Then: WorkSpaceNotFoundException이 발생해야 함
        assertThatThrownBy(() -> workSpaceService.getWorkSpaceEntityOrThrow(VALID_WORKSPACE_ID))
                .isInstanceOf(WorkSpaceNotFoundException.class);
        
        // Then: Repository 메서드가 정확히 한 번 호출되어야 함
        then(workspaceRepository).should(times(1)).findById(VALID_WORKSPACE_ID);
    }
}

