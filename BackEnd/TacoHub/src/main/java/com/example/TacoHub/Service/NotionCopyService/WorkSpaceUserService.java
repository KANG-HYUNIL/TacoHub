package com.example.TacoHub.Service.NotionCopyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceUserEntity;
import com.example.TacoHub.Enum.NotionCopyEnum.MembershipStatus;
import com.example.TacoHub.Repository.NotionCopyRepository.WorkSpaceUserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkSpaceUserService {

    // User-Workspace 관계 담은 WorkSpaceUserRepository 종속성성
    private final WorkSpaceUserRepository workSpaceUserRepository;



    /**
     * 사용자-워크스페이스 관계 조회
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     * @return WorkSpaceUserEntity (Optional)
     */
    private Optional<WorkSpaceUserEntity> getWorkSpaceUserEntity(String userEmailId, UUID workspaceId) {
        try {
            // 입력값 검증
            if (userEmailId == null || userEmailId.trim().isEmpty()) {
                log.warn("사용자 이메일 ID가 비어있음");
                return Optional.empty();
            }
            if (workspaceId == null) {
                log.warn("워크스페이스 ID가 null");
                return Optional.empty();
            }

            return workSpaceUserRepository.findByUser_EmailIdAndWorkspace_Id(userEmailId.trim(), workspaceId);
            
        } catch (Exception e) {
            log.error("WorkSpaceUserEntity 조회 실패: userEmailId={}, workspaceId={}, error={}", 
                    userEmailId, workspaceId, e.getMessage());
            return Optional.empty();
        }
    }



    /**
     * 사용자가 워크스페이스를 관리할 수 있는지 확인
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     * @return 관리 권한 여부
     */
    public boolean canUserManageWorkSpace(String userEmailId, UUID workspaceId) {
        try {
            log.debug("워크스페이스 관리 권한 확인: userEmailId={}, workspaceId={}", userEmailId, workspaceId);
            
            // 입력값 검증
            if (userEmailId == null || userEmailId.trim().isEmpty() || workspaceId == null) {
                log.warn("잘못된 입력값으로 인한 권한 거부: userEmailId={}, workspaceId={}", userEmailId, workspaceId);
                return false;
            }

            Optional<WorkSpaceUserEntity> result = getWorkSpaceUserEntity(userEmailId, workspaceId);
            
            // 사용자-워크스페이스 관계가 존재하지 않음
            if (result.isEmpty()) {
                // 🔐 보안 이벤트: 권한 없는 사용자의 접근 시도
                log.warn("권한 없는 워크스페이스 관리 시도 - 사용자 관계 없음: userEmailId={}, workspaceId={}", 
                        userEmailId, workspaceId);
                return false;
            }

            boolean canManage = result.filter(entity -> entity.getMembershipStatus() == MembershipStatus.ACTIVE)
                                    .map(entity -> entity.getWorkspaceRole().canManageWorkspace())
                                    .orElse(false);
            
            // 🔐 보안 이벤트: 권한 부족으로 인한 거부
            if (!canManage) {
                log.warn("권한 부족으로 인한 워크스페이스 관리 거부: userEmailId={}, workspaceId={}, " +
                        "membershipStatus={}, role={}", 
                        userEmailId, workspaceId, 
                        result.get().getMembershipStatus(),
                        result.get().getWorkspaceRole());
            } else {
                // ✅ 비즈니스 이벤트: 정상적인 관리 권한 승인
                log.info("워크스페이스 관리 권한 승인: userEmailId={}, workspaceId={}", 
                        userEmailId, workspaceId);
            }
            
            return canManage;
            
        } catch (Exception e) {
            log.error("워크스페이스 관리 권한 확인 실패: userEmailId={}, workspaceId={}, error={}", 
                    userEmailId, workspaceId, e.getMessage());
            return false;
        }
    }

    /**
     * 사용자가 워크스페이스에서 사용자 초대/삭제 권한이 있는지 확인
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     * @return 초대/삭제 권한 여부
     */
    public boolean canUserInviteAndDeleteUsers(String userEmailId, UUID workspaceId) {
        return checkUserPermission(userEmailId, workspaceId, "invite/delete users", 
                entity -> entity.getWorkspaceRole().canInviteAndDeleteUsers());
    }

    /**
     * 사용자가 워크스페이스에서 페이지 삭제 권한이 있는지 확인
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     * @return 페이지 삭제 권한 여부
     */
    public boolean canUserDeletePage(String userEmailId, UUID workspaceId) {
        return checkUserPermission(userEmailId, workspaceId, "delete page", 
                entity -> entity.getWorkspaceRole().canDeletePage());
    }

    /**
     * 사용자가 워크스페이스에서 페이지 편집/생성 권한이 있는지 확인
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     * @return 페이지 편집/생성 권한 여부
     */
    public boolean canUserEditPage(String userEmailId, UUID workspaceId) {
        return checkUserPermission(userEmailId, workspaceId, "edit page", 
                entity -> entity.getWorkspaceRole().canEditPage());
    }

    /**
     * 사용자가 워크스페이스에서 페이지 조회 권한이 있는지 확인
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     * @return 페이지 조회 권한 여부
     */
    public boolean canUserViewPage(String userEmailId, UUID workspaceId) {
        return checkUserPermission(userEmailId, workspaceId, "view page", 
                entity -> entity.getWorkspaceRole().canViewPage());
    }

    /**
     * 공통 권한 체크 로직
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     * @param permissionName 권한 이름 (로깅용)
     * @param permissionChecker 권한 체크 함수
     * @return 권한 여부
     */
    private boolean checkUserPermission(String userEmailId, UUID workspaceId, String permissionName, 
                                      java.util.function.Function<WorkSpaceUserEntity, Boolean> permissionChecker) {
        try {
            log.debug("{} 권한 확인: userEmailId={}, workspaceId={}", permissionName, userEmailId, workspaceId);
            
            // 입력값 검증
            if (userEmailId == null || userEmailId.trim().isEmpty() || workspaceId == null) {
                log.warn("잘못된 입력값으로 인한 {} 권한 거부: userEmailId={}, workspaceId={}", 
                        permissionName, userEmailId, workspaceId);
                return false;
            }

            Optional<WorkSpaceUserEntity> result = getWorkSpaceUserEntity(userEmailId, workspaceId);
            
            // 사용자-워크스페이스 관계가 존재하지 않음
            if (result.isEmpty()) {
                // 🔐 보안 이벤트: 관계 없는 사용자의 접근 시도
                log.warn("권한 없는 {} 시도 - 사용자 관계 없음: userEmailId={}, workspaceId={}", 
                        permissionName, userEmailId, workspaceId);
                return false;
            }

            boolean hasPermission = result.filter(entity -> entity.getMembershipStatus() == MembershipStatus.ACTIVE)
                                          .map(permissionChecker)
                                          .orElse(false);
            
            // 권한 결과에 따른 로깅
            if (!hasPermission) {
                // 🔐 보안 이벤트: 권한 부족으로 인한 거부
                log.warn("권한 부족으로 인한 {} 거부: userEmailId={}, workspaceId={}, " +
                        "membershipStatus={}, role={}", 
                        permissionName, userEmailId, workspaceId,
                        result.get().getMembershipStatus(),
                        result.get().getWorkspaceRole());
            }
            
            return hasPermission;
            
        } catch (Exception e) {
            log.error("{} 권한 확인 실패: userEmailId={}, workspaceId={}, error={}", 
                    permissionName, userEmailId, workspaceId, e.getMessage());
            return false;
        }
    }

}

