package com.example.TacoHub.Service.NotionCopyService;

import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceUserEntity;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceEntity;
import com.example.TacoHub.Repository.NotionCopyRepository.WorkSpaceUserRepository;
import com.example.TacoHub.Enum.NotionCopyEnum.MembershipStatus;
import com.example.TacoHub.Enum.NotionCopyEnum.WorkSpaceRole;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkSpaceUserService 테스트")
public class WorkSpaceUserServiceTest {
    
    @Mock
    private WorkSpaceUserRepository workSpaceUserRepository;
    
    @InjectMocks
    private WorkSpaceUserService workSpaceUserService;

    @Test
    @DisplayName("사용자 워크스페이스 관리 권한 확인 성공 테스트 - OWNER")
    void canUserManageWorkSpace_UserIsOwner_ReturnsTrue() {
        // Given - OWNER 권한을 가진 사용자 설정
        String userEmailId = "owner@test.com";
        UUID workspaceId = UUID.randomUUID();
        
        // OWNER 권한을 가진 활성 상태의 WorkSpaceUserEntity 모의 객체 생성
        WorkSpaceUserEntity mockWorkSpaceUser = WorkSpaceUserEntity.builder()
                .workspaceRole(WorkSpaceRole.OWNER) // OWNER 권한 설정
                .membershipStatus(MembershipStatus.ACTIVE) // 활성 상태 설정
                .build();
        
        // Repository가 해당 사용자와 워크스페이스로 조회 시 위의 엔티티를 반환하도록 모의 설정
        when(workSpaceUserRepository.findByUser_EmailIdAndWorkspace_Id(userEmailId, workspaceId))
                .thenReturn(Optional.of(mockWorkSpaceUser));
        
        // When - 워크스페이스 관리 권한 확인 메서드 실행
        boolean result = workSpaceUserService.canUserManageWorkSpace(userEmailId, workspaceId);
        
        // Then - OWNER는 워크스페이스 관리 권한이 있으므로 true 반환 예상
        assertThat(result).isTrue();
        
        // Repository의 메서드가 올바른 매개변수로 호출되었는지 검증
        verify(workSpaceUserRepository).findByUser_EmailIdAndWorkspace_Id(userEmailId, workspaceId);
    }

    @Test
    @DisplayName("사용자 워크스페이스 관리 권한 확인 실패 테스트 - 사용자 없음")
    void canUserManageWorkSpace_UserNotExists_ReturnsFalse() {
        // Given - 존재하지 않는 사용자 설정
        String nonExistentUserEmail = "nonexistent@test.com";
        UUID workspaceId = UUID.randomUUID();
        
        // Repository가 해당 사용자로 조회 시 빈 Optional을 반환하도록 모의 설정
        when(workSpaceUserRepository.findByUser_EmailIdAndWorkspace_Id(nonExistentUserEmail, workspaceId))
                .thenReturn(Optional.empty());
        
        // When - 워크스페이스 관리 권한 확인 메서드 실행
        boolean result = workSpaceUserService.canUserManageWorkSpace(nonExistentUserEmail, workspaceId);
        
        // Then - 존재하지 않는 사용자는 권한이 없으므로 false 반환 예상
        assertThat(result).isFalse();
        
        // Repository의 메서드가 올바른 매개변수로 호출되었는지 검증
        verify(workSpaceUserRepository).findByUser_EmailIdAndWorkspace_Id(nonExistentUserEmail, workspaceId);
    }

    @Test
    @DisplayName("사용자 페이지 편집 권한 확인 성공 테스트 - EDITOR")
    void canUserEditPage_UserIsEditor_ReturnsTrue() {
        // Given - EDITOR 권한을 가진 사용자 설정
        String userEmailId = "editor@test.com";
        UUID workspaceId = UUID.randomUUID();
        
        // ADMIN 권한을 가진 활성 상태의 WorkSpaceUserEntity 모의 객체 생성
        WorkSpaceUserEntity mockWorkSpaceUser = WorkSpaceUserEntity.builder()
                .workspaceRole(WorkSpaceRole.ADMIN) // ADMIN 권한 설정
                .membershipStatus(MembershipStatus.ACTIVE) // 활성 상태 설정
                .build();
        
        // Repository 모의 설정
        when(workSpaceUserRepository.findByUser_EmailIdAndWorkspace_Id(userEmailId, workspaceId))
                .thenReturn(Optional.of(mockWorkSpaceUser));
        
        // When - 페이지 편집 권한 확인 메서드 실행
        boolean result = workSpaceUserService.canUserEditPage(userEmailId, workspaceId);
        
        // Then - ADMIN은 페이지 편집 권한이 있으므로 true 반환 예상
        assertThat(result).isTrue();
        
        // Repository 메서드 호출 검증
        verify(workSpaceUserRepository).findByUser_EmailIdAndWorkspace_Id(userEmailId, workspaceId);
    }

    @Test
    @DisplayName("사용자 페이지 편집 권한 확인 실패 테스트 - GUEST")
    void canUserEditPage_UserIsGuest_ReturnsFalse() {
        // Given - GUEST 권한을 가진 사용자 설정
        String userEmailId = "guest@test.com";
        UUID workspaceId = UUID.randomUUID();
        
        // GUEST 권한을 가진 활성 상태의 WorkSpaceUserEntity 모의 객체 생성
        WorkSpaceUserEntity mockWorkSpaceUser = WorkSpaceUserEntity.builder()
                .workspaceRole(WorkSpaceRole.GUEST) // GUEST 권한 설정 (편집 권한 없음)
                .membershipStatus(MembershipStatus.ACTIVE) // 활성 상태 설정
                .build();
        
        // Repository 모의 설정
        when(workSpaceUserRepository.findByUser_EmailIdAndWorkspace_Id(userEmailId, workspaceId))
                .thenReturn(Optional.of(mockWorkSpaceUser));
        
        // When - 페이지 편집 권한 확인 메서드 실행
        boolean result = workSpaceUserService.canUserEditPage(userEmailId, workspaceId);
        
        // Then - GUEST는 페이지 편집 권한이 없으므로 false 반환 예상
        assertThat(result).isFalse();
        
        // Repository 메서드 호출 검증
        verify(workSpaceUserRepository).findByUser_EmailIdAndWorkspace_Id(userEmailId, workspaceId);
    }

    @Test
    @DisplayName("사용자 페이지 삭제 권한 확인 성공 테스트 - OWNER")
    void canUserDeletePage_UserIsOwner_ReturnsTrue() {
        // Given - OWNER 권한을 가진 사용자 설정
        String userEmailId = "owner@test.com";
        UUID workspaceId = UUID.randomUUID();
        
        // OWNER 권한을 가진 활성 상태의 WorkSpaceUserEntity 모의 객체 생성
        WorkSpaceUserEntity mockWorkSpaceUser = WorkSpaceUserEntity.builder()
                .workspaceRole(WorkSpaceRole.OWNER) // OWNER 권한 설정 (모든 권한 보유)
                .membershipStatus(MembershipStatus.ACTIVE) // 활성 상태 설정
                .build();
        
        // Repository 모의 설정
        when(workSpaceUserRepository.findByUser_EmailIdAndWorkspace_Id(userEmailId, workspaceId))
                .thenReturn(Optional.of(mockWorkSpaceUser));
        
        // When - 페이지 삭제 권한 확인 메서드 실행
        boolean result = workSpaceUserService.canUserDeletePage(userEmailId, workspaceId);
        
        // Then - OWNER는 페이지 삭제 권한이 있으므로 true 반환 예상
        assertThat(result).isTrue();
        
        // Repository 메서드 호출 검증
        verify(workSpaceUserRepository).findByUser_EmailIdAndWorkspace_Id(userEmailId, workspaceId);
    }

    @Test
    @DisplayName("사용자 페이지 삭제 권한 확인 성공 테스트 - ADMIN")
    void canUserDeletePage_UserIsAdmin_ReturnsFalse() {
        // Given - ADMIN 권한을 가진 사용자 설정
        String userEmailId = "admin@test.com";
        UUID workspaceId = UUID.randomUUID();
        
        // ADMIN 권한을 가진 활성 상태의 WorkSpaceUserEntity 모의 객체 생성
        WorkSpaceUserEntity mockWorkSpaceUser = WorkSpaceUserEntity.builder()
                .workspaceRole(WorkSpaceRole.ADMIN) // ADMIN 권한 설정 (삭제 권한 없음)
                .membershipStatus(MembershipStatus.ACTIVE) // 활성 상태 설정
                .build();
        
        // Repository 모의 설정
        when(workSpaceUserRepository.findByUser_EmailIdAndWorkspace_Id(userEmailId, workspaceId))
                .thenReturn(Optional.of(mockWorkSpaceUser));
        
        // When - 페이지 삭제 권한 확인 메서드 실행
        boolean result = workSpaceUserService.canUserDeletePage(userEmailId, workspaceId);
        
        // Then - ADMIN은 페이지 삭제 권한 있으므로 true 예상
        assertThat(result).isTrue();
        
        // Repository 메서드 호출 검증
        verify(workSpaceUserRepository).findByUser_EmailIdAndWorkspace_Id(userEmailId, workspaceId);
    }

    @Test
    @DisplayName("사용자 페이지 조회 권한 확인 성공 테스트 - GUEST")
    void canUserViewPage_UserIsGuest_ReturnsTrue() {
        // Given - GUEST 권한을 가진 사용자 설정
        String userEmailId = "guest@test.com";
        UUID workspaceId = UUID.randomUUID();
        
        // GUEST 권한을 가진 활성 상태의 WorkSpaceUserEntity 모의 객체 생성
        WorkSpaceUserEntity mockWorkSpaceUser = WorkSpaceUserEntity.builder()
                .workspaceRole(WorkSpaceRole.GUEST) // GUEST 권한 설정 (조회 권한 보유)
                .membershipStatus(MembershipStatus.ACTIVE) // 활성 상태 설정
                .build();
        
        // Repository 모의 설정
        when(workSpaceUserRepository.findByUser_EmailIdAndWorkspace_Id(userEmailId, workspaceId))
                .thenReturn(Optional.of(mockWorkSpaceUser));
        
        // When - 페이지 조회 권한 확인 메서드 실행
        boolean result = workSpaceUserService.canUserViewPage(userEmailId, workspaceId);
        
        // Then - GUEST는 페이지 조회 권한이 있으므로 true 반환 예상
        assertThat(result).isTrue();
        
        // Repository 메서드 호출 검증
        verify(workSpaceUserRepository).findByUser_EmailIdAndWorkspace_Id(userEmailId, workspaceId);
    }

    @Test
    @DisplayName("사용자 멤버십 상태가 비활성화일 때 권한 확인 실패 테스트")
    void canUserEditPage_UserIsInactive_ReturnsFalse() {
        // Given - 비활성화 상태의 사용자 설정
        String userEmailId = "inactive@test.com";
        UUID workspaceId = UUID.randomUUID();
        
        // OWNER 권한이지만 비활성화 상태의 WorkSpaceUserEntity 모의 객체 생성
        WorkSpaceUserEntity mockWorkSpaceUser = WorkSpaceUserEntity.builder()
                .workspaceRole(WorkSpaceRole.OWNER) // OWNER 권한이지만
                .membershipStatus(MembershipStatus.SUSPENDED) // 정지 상태
                .build();
        
        // Repository 모의 설정
        when(workSpaceUserRepository.findByUser_EmailIdAndWorkspace_Id(userEmailId, workspaceId))
                .thenReturn(Optional.of(mockWorkSpaceUser));
        
        // When - 페이지 편집 권한 확인 메서드 실행
        boolean result = workSpaceUserService.canUserEditPage(userEmailId, workspaceId);
        
        // Then - 비활성화 상태이므로 권한이 없어 false 반환 예상
        assertThat(result).isFalse();
        
        // Repository 메서드 호출 검증
        verify(workSpaceUserRepository).findByUser_EmailIdAndWorkspace_Id(userEmailId, workspaceId);
    }
}
