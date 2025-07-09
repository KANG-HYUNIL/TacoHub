package com.example.TacoHub.Service.NotionCopyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.TacoHub.Entity.AccountEntity;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceEntity;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceUserEntity;
import com.example.TacoHub.Enum.NotionCopyEnum.MembershipStatus;
import com.example.TacoHub.Enum.NotionCopyEnum.WorkSpaceRole;
import com.example.TacoHub.Exception.AccountNotFoundException;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceNotFoundException;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceUserNotFoundException;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceUserOperationException;
import com.example.TacoHub.Logging.UserInfoExtractor;
import com.example.TacoHub.Repository.NotionCopyRepository.WorkSpaceUserRepository;
import com.example.TacoHub.Service.AccountService;

import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkSpaceUserService {

    // User-Workspace 관계 담은 WorkSpaceUserRepository 종속성성
    private final WorkSpaceUserRepository workSpaceUserRepository;

    private final WorkSpaceService workSpaceService;
    private final AccountService accountService;
    private final UserInfoExtractor userInfoExtractor;

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
     * Role이 Admin, Status는 Actie인 WorkSpaceUserEntity를 생성하는 Method
     * @param userEmailId
     * @param workspaceId
     * @return 생성된 WorkSpaceUserEntity
     */
    public WorkSpaceUserEntity createAdminUserEntity (
        String userEmailId,
        UUID workspaceId
    )
    {
        try {
            //입력값 검증
            if (userEmailId == null || userEmailId.trim().isEmpty()) {
                log.warn("사용자 이메일 ID 비어있음");
                throw new WorkSpaceUserOperationException("사용자 이메일 ID는 필수입니다");
                
            }

            if (workspaceId == null) {
                log.warn("워크스페이스 ID가 null");
                throw new WorkSpaceUserOperationException("워크스페이스 ID는 필수입니다");
            }

            // 사용자-워크스페이스 관계가 이미 존재하는지 확인
            Optional<WorkSpaceUserEntity> existingEntity = getWorkSpaceUserEntity(userEmailId, workspaceId);
            if (existingEntity.isPresent()) {
                log.warn("이미 존재하는 사용자-워크스페이스 관계: userEmailId={}, workspaceId={}", 
                        userEmailId, workspaceId);
                throw new WorkSpaceUserOperationException(
                    "이미 존재하는 사용자-워크스페이스 관계입니다: userEmailId=" + userEmailId + ", workspaceId=" + workspaceId);
            }

            //WorkSpaceUserEntity가 이미 존재하는지 확인
            // 워크스페이스와 사용자 엔티티 조회
            WorkSpaceEntity workspace = workSpaceService.getWorkSpaceEntityOrThrow(workspaceId);
            AccountEntity user = accountService.getAccountEntityOrThrow(userEmailId);

            // Admin으로 등록하는 WorkSpaceUserEntity 생성
            WorkSpaceUserEntity newUser = WorkSpaceUserFactory.createOwnerEntity(workspace, user);
            
            // 저장 후 반환
            return workSpaceUserRepository.save(newUser);


        } 
        catch(WorkSpaceUserOperationException e) {
            handleAndThrowWorkSpaceUserException("createAdminUserEntity", e);
            return null;
        } catch(WorkSpaceNotFoundException e) {
            handleAndThrowWorkSpaceUserException("createAdminUserEntity", e);
            return null;
        } catch(AccountNotFoundException e) {
            handleAndThrowWorkSpaceUserException("createAdminUserEntity", e);
            return null;
        } catch(Exception e) {
            handleAndThrowWorkSpaceUserException("createAdminUserEntity", e);
            return null;
        }
        
    }

    /**
     * 해당 workspaceId의 WorkSpaceUserEntity를 모두 삭제하는 method
     * @param workspaceId
     */
    @Transactional
    public void deleteWorkSpaceUserAllEntites(
        UUID workspaceId
    ) 
    {
        try {

            // 입력값 검증
            if (workspaceId == null) {
                log.warn("워크스페이스 ID가 null");
                throw new WorkSpaceUserOperationException("워크스페이스 ID는 필수입니다");
            }

            workSpaceUserRepository.deleteByWorkspace_Id(workspaceId);
            
        } 
        catch (WorkSpaceUserNotFoundException e) 
        {
            handleAndThrowWorkSpaceUserException("deleteWorkSpaceUserEntites", e);
        } 
        catch (Exception e) 
        {
            handleAndThrowWorkSpaceUserException("deleteWorkSpaceUserEntites", e);
        }
    }
    

    /**
     * WorkSpaceUserEntity의 Role를  Update 하는 Method
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     * @param newRole 새로운 멤버십 상태
     */
    @Transactional
    public void updateUserRole(
        String userEmailId,
        UUID workspaceId,
        WorkSpaceRole newRole
    )
    {
        try {
            Optional<WorkSpaceUserEntity> optionalEntity = getWorkSpaceUserEntity(userEmailId, workspaceId);
            
            // 유효성 검증
            if (optionalEntity.isEmpty()) 
            {
                log.warn("사용자 {}와 워크스페이스 {}의 관계가 존재하지 않습니다.", userEmailId, workspaceId);
                throw new WorkSpaceUserNotFoundException("사용자와 워크스페이스의 관계가 존재하지 않습니다 : userEmailId=" + userEmailId + ", workspaceId=" + workspaceId);
            }

            // 권한 검증
            String currentUserEmail = userInfoExtractor.getCurrentUserId();
            if (!canUserInviteAndDeleteUsers(currentUserEmail, workspaceId))
            {
                log.warn("사용자 {}는 워크스페이스 {}에서 사용자 초대 및 삭제 권한이 없습니다.", currentUserEmail, workspaceId);
                throw new WorkSpaceUserOperationException("사용자 초대 및 삭제 권한이 없습니다: userEmailId=" + currentUserEmail + ", workspaceId=" + workspaceId);
            }

            WorkSpaceUserEntity entity = optionalEntity.get();
            // 현재 Role과 동일한 경우 업데이트 하지 않음
            if (entity.getWorkspaceRole() == newRole) 
            {
                log.info("사용자 {}의 워크스페이스 {} Role이 이미 {}입니다. 업데이트하지 않습니다.",
                        userEmailId, workspaceId, newRole);
                return;
            }
            // Role 업데이트
            entity.setWorkspaceRole(newRole);
            workSpaceUserRepository.save(entity);
            log.info("사용자 {}의 워크스페이스 {} Role을 {}로 업데이트했습니다.",
                    userEmailId, workspaceId, newRole);

        } 
        catch (WorkSpaceUserNotFoundException e) 
        {
            log.warn("사용자 {}의 워크스페이스 {} Role 업데이트 실패: {}", userEmailId, workspaceId, e.getMessage());
            throw e; // 비즈니스 예외는 그대로 전파
        }
        catch (Exception e) 
        {
            handleAndThrowWorkSpaceUserException("updateUserRole", e);

        }

    }



    /**
     * WorkSpaceUserEntity의 MembershipStatus Update 하는 Method
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     * @param newMembershipStatus 새로운 멤버십 상태
     */
    public void updateMembershipStatus(
        String userEmailId,
        UUID workspaceId,
        MembershipStatus newMembershipStatus
    )
    {
        try {
            Optional<WorkSpaceUserEntity> optionalEntity = getWorkSpaceUserEntity(userEmailId, workspaceId);
            
            if (optionalEntity.isEmpty()) 
            {
                log.warn("사용자 {}와 워크스페이스 {}의 관계가 존재하지 않습니다.", userEmailId, workspaceId);
                throw new WorkSpaceUserNotFoundException("사용자와 워크스페이스의 관계가 존재하지 않습니다 : userEmailId=" + userEmailId + ", workspaceId=" + workspaceId);
            }

            WorkSpaceUserEntity entity = optionalEntity.get();
            // 현재 Role과 동일한 경우 업데이트 하지 않음
            if (entity.getMembershipStatus() == newMembershipStatus) 
            {
                log.info("사용자 {}의 워크스페이스 {} Role이 이미 {}입니다. 업데이트하지 않습니다.",
                        userEmailId, workspaceId, newMembershipStatus);
                return;
            }
            // Role 업데이트
            entity.setMembershipStatus(newMembershipStatus);
            workSpaceUserRepository.save(entity);
            log.info("사용자 {}의 워크스페이스 {} Role을 {}로 업데이트했습니다.",
                    userEmailId, workspaceId, newMembershipStatus);

        } 
        catch (WorkSpaceUserNotFoundException e) 
        {
            log.warn("사용자 {}의 워크스페이스 {} MembershipStatus 업데이트 실패: {}", userEmailId, workspaceId, e.getMessage());
            throw e; // 비즈니스 예외는 그대로 전파
        }
        catch (Exception e) 
        {
            handleAndThrowWorkSpaceUserException("updateMembershipStatus", e);
            
        }
    }


    /**
     * WorkSpaceuserEntity의 Role을 Admin으로 변경하는 Method
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     */
    public void promoteToAdmin(
        String userEmailId,
        UUID workspaceId
    ) 
    {
        updateUserRole(userEmailId, workspaceId, WorkSpaceRole.ADMIN);
    }
    
    

    /**
     * WorkSpaceuserEntity의 Role를 Member으로 변경하는 Method
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     */
    public void promoteToMember(
        String userEmailId,
        UUID workspaceId
    )
    {
        updateUserRole(userEmailId, workspaceId, WorkSpaceRole.MEMBER);
    }

    
    
    /**
     * WorkSpaceuserEntity의 Role를 Guest로 변경하는 Method
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     */
    public void promoteToGuest(
        String userEmailId,
        UUID workspaceId
    )
    {
        updateUserRole(userEmailId, workspaceId, WorkSpaceRole.GUEST);
    }
     

    /**
     * WorkSpaceuserEntity의 MembershipStatus를 Active으로 변경하는 Method
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     */
    public void activateUser(
        String userEmailId,
        UUID workspaceId
    )
    {
        updateMembershipStatus(userEmailId, workspaceId, MembershipStatus.ACTIVE);
    }


    /**
     * WorkSpaceuserEntity의 MembershipStatus를 Suspended로 변경하는 Method
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     */
    public void suspendUser(
        String userEmailId,
        UUID workspaceId
    )
    {
        updateMembershipStatus(userEmailId, workspaceId, MembershipStatus.SUSPENDED);
    }


    /**
     * Admin으로 초대하고 WorkSpaceUserEntity를 생성하는 Method
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     */
    public WorkSpaceUserEntity inviteAsAdmin(
        String userEmailId,
        UUID workspaceId
    )
    {
        try {
            // 입력값 검증
            if (userEmailId == null || userEmailId.trim().isEmpty()) {
                log.warn("사용자 이메일 ID 비어있음");
                throw new WorkSpaceUserOperationException("사용자 이메일 ID는 필수입니다");
            }
            if (workspaceId == null) {
                log.warn("워크스페이스 ID가 null");
                throw new WorkSpaceUserOperationException("워크스페이스 ID는 필수입니다");
            }

            // 사용자-워크스페이스 관계가 이미 존재하는지 확인
            Optional<WorkSpaceUserEntity> existingEntity = getWorkSpaceUserEntity(userEmailId, workspaceId);
            if (existingEntity.isPresent()) {
                log.warn("이미 존재하는 사용자-워크스페이스 관계: userEmailId={}, workspaceId={}", 
                        userEmailId, workspaceId);
                throw new WorkSpaceUserOperationException(
                    "이미 존재하는 사용자-워크스페이스 관계입니다: userEmailId=" + userEmailId + ", workspaceId=" + workspaceId);
            }

            // 워크스페이스와 사용자 엔티티 조회
            WorkSpaceEntity workspace = workSpaceService.getWorkSpaceEntityOrThrow(workspaceId);
            AccountEntity user = accountService.getAccountEntityOrThrow(userEmailId);

            // Admin으로 초대하는 WorkSpaceUserEntity 생성
            WorkSpaceUserEntity invitedUser = WorkSpaceUserFactory.createdInvitedAdminEntity(workspace, user);
            
            // 저장 후 반환
            return workSpaceUserRepository.save(invitedUser);
        } catch (WorkSpaceUserOperationException e) {
            handleAndThrowWorkSpaceUserException("inviteAsAdmin", e);
            return null;
        } catch (WorkSpaceNotFoundException e) {
            handleAndThrowWorkSpaceUserException("inviteAsAdmin", e);
            return null;
        } catch (AccountNotFoundException e) {
            handleAndThrowWorkSpaceUserException("inviteAsAdmin", e);
            return null;
        } catch (Exception e) {
            handleAndThrowWorkSpaceUserException("inviteAsAdmin", e);
            return null;
        }
    }


    /**
     * Member로 초대하고 WorkSpaceUserEntity를 생성하는 Method
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     */
    public WorkSpaceUserEntity inviteAsMember(
        String userEmailId,
        UUID workspaceId
    )
    {
        try {
            // 입력값 검증
            if (userEmailId == null || userEmailId.trim().isEmpty()) {
                log.warn("사용자 이메일 ID 비어있음");
                throw new WorkSpaceUserOperationException("사용자 이메일 ID는 필수입니다");
            }
            if (workspaceId == null) {
                log.warn("워크스페이스 ID가 null");
                throw new WorkSpaceUserOperationException("워크스페이스 ID는 필수입니다");
            }

            // 사용자-워크스페이스 관계가 이미 존재하는지 확인
            Optional<WorkSpaceUserEntity> existingEntity = getWorkSpaceUserEntity(userEmailId, workspaceId);
            if (existingEntity.isPresent()) {
                log.warn("이미 존재하는 사용자-워크스페이스 관계: userEmailId={}, workspaceId={}", 
                        userEmailId, workspaceId);
                throw new WorkSpaceUserOperationException(
                    "이미 존재하는 사용자-워크스페이스 관계입니다: userEmailId=" + userEmailId + ", workspaceId=" + workspaceId);
            }

            // 워크스페이스와 사용자 엔티티 조회
            WorkSpaceEntity workspace = workSpaceService.getWorkSpaceEntityOrThrow(workspaceId);
            AccountEntity user = accountService.getAccountEntityOrThrow(userEmailId);

            // Member로 초대하는 WorkSpaceUserEntity 생성
            WorkSpaceUserEntity invitedUser = WorkSpaceUserFactory.createdInvitedMemberEntity(workspace, user);
            
            // 저장 후 반환
            return workSpaceUserRepository.save(invitedUser);
        } catch (WorkSpaceUserOperationException e) {
            handleAndThrowWorkSpaceUserException("inviteAsMember", e);
            return null;
        } catch (WorkSpaceNotFoundException e) {
            handleAndThrowWorkSpaceUserException("inviteAsMember", e);
            return null;
        } catch (AccountNotFoundException e) {
            handleAndThrowWorkSpaceUserException("inviteAsMember", e);
            return null;
        } catch (Exception e) {
            handleAndThrowWorkSpaceUserException("inviteAsMember", e);
            return null;
        }
    }

    /**
     * Guest로 초대하고 WorkSpaceUserEntity를 생성하는 Method
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     */
    public WorkSpaceUserEntity inviteAsGuest(
        String userEmailId,
        UUID workspaceId
    )
    {
        try {
            // 입력값 검증
            if (userEmailId == null || userEmailId.trim().isEmpty()) {
                log.warn("사용자 이메일 ID 비어있음");
                throw new WorkSpaceUserOperationException("사용자 이메일 ID는 필수입니다");
            }
            if (workspaceId == null) {
                log.warn("워크스페이스 ID가 null");
                throw new WorkSpaceUserOperationException("워크스페이스 ID는 필수입니다");
            }

            // 사용자-워크스페이스 관계가 이미 존재하는지 확인
            Optional<WorkSpaceUserEntity> existingEntity = getWorkSpaceUserEntity(userEmailId, workspaceId);
            if (existingEntity.isPresent()) {
                log.warn("이미 존재하는 사용자-워크스페이스 관계: userEmailId={}, workspaceId={}", 
                        userEmailId, workspaceId);
                throw new WorkSpaceUserOperationException(
                    "이미 존재하는 사용자-워크스페이스 관계입니다: userEmailId=" + userEmailId + ", workspaceId=" + workspaceId);
            }

            // 워크스페이스와 사용자 엔티티 조회
            WorkSpaceEntity workspace = workSpaceService.getWorkSpaceEntityOrThrow(workspaceId);
            AccountEntity user = accountService.getAccountEntityOrThrow(userEmailId);

            // Guest로 초대하는 WorkSpaceUserEntity 생성
            WorkSpaceUserEntity invitedUser = WorkSpaceUserFactory.createdInvitedGuestEntity(workspace, user);
            
            // 저장 후 반환
            return workSpaceUserRepository.save(invitedUser);
        } catch (WorkSpaceUserOperationException e) {
            handleAndThrowWorkSpaceUserException("inviteAsGuest", e);
            return null;
        } catch (WorkSpaceNotFoundException e) {
            handleAndThrowWorkSpaceUserException("inviteAsGuest", e);
            return null;
        } catch (AccountNotFoundException e) {
            handleAndThrowWorkSpaceUserException("inviteAsGuest", e);
            return null;
        } catch (Exception e) {
            handleAndThrowWorkSpaceUserException("inviteAsGuest", e);
            return null;
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

            handleAndThrowWorkSpaceUserException(permissionName, e);
            return false; // 실제로는 도달하지 않음
        }
    }


    
    /**
    * 공통 AccountService 예외 처리 메서드
    * @param methodName 실패한 메서드명
    * @param originalException 원본 예외
    * @throws WorkSpaceUserOperationException 래핑된 예외
    */
    private void handleAndThrowWorkSpaceUserException(String methodName, Exception originalException) {
        String errorMessage = originalException.getMessage();
        String exceptionType = originalException.getClass().getSimpleName();
        log.error("{} 실패: type={}, message={}", methodName, exceptionType, errorMessage
        , originalException);
        throw new WorkSpaceUserOperationException(
            String.format("%s 실패 [%s]: %s", methodName, exceptionType, errorMessage),
            originalException
        );  
    }

}

