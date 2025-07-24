package com.example.TacoHub.Service.NotionCopyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.example.TacoHub.Entity.AccountEntity;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceEntity;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceUserEntity;
import com.example.TacoHub.Enum.NotionCopyEnum.MembershipStatus;
import com.example.TacoHub.Enum.NotionCopyEnum.WorkSpaceRole;
import com.example.TacoHub.Exception.BusinessException;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceUserNotFoundException;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceUserOperationException;
import com.example.TacoHub.Logging.AuditLogging;
import com.example.TacoHub.Logging.UserInfoExtractor;
import com.example.TacoHub.Repository.NotionCopyRepository.WorkSpaceUserRepository;
import com.example.TacoHub.Service.AccountService;
import com.example.TacoHub.Service.BaseService;

import jakarta.transaction.Transactional;

@Service
@Slf4j
public class WorkSpaceUserService extends BaseService {

    // User-Workspace 관계 담은 WorkSpaceUserRepository 종속성성
    private final WorkSpaceUserRepository workSpaceUserRepository;

    private final WorkSpaceService workSpaceService;
    private final AccountService accountService;
    private final UserInfoExtractor userInfoExtractor;

    public WorkSpaceUserService(
        WorkSpaceUserRepository workSpaceUserRepository,
        @Lazy WorkSpaceService workSpaceService,
        AccountService accountService,
        UserInfoExtractor userInfoExtractor
    ) {
        this.workSpaceUserRepository = workSpaceUserRepository;
        this.workSpaceService = workSpaceService;
        this.accountService = accountService;
        this.userInfoExtractor = userInfoExtractor;
    }

    // ===== 입력값 검증 메서드 =====
    
    /**
     * 사용자 이메일 ID 검증
     * @param userEmailId 검증할 사용자 이메일 ID
     * @param methodName 호출한 메서드명 (로깅용)
     */
    private void validateUserEmailId(String userEmailId, String methodName) {
        if (isStringNullOrEmpty(userEmailId)) {
            log.warn("사용자 이메일 ID 검증 실패: 메서드={}, 원인=사용자 이메일 ID는 필수입니다", methodName);
            throw new WorkSpaceUserOperationException("사용자 이메일 ID는 필수입니다");
        }
    }

    /**
     * 워크스페이스 ID 검증
     * @param workspaceId 검증할 워크스페이스 ID
     * @param methodName 호출한 메서드명 (로깅용)
     */
    private void validateWorkspaceId(UUID workspaceId, String methodName) {
        if (isNull(workspaceId)) {
            log.warn("워크스페이스 ID 검증 실패: 메서드={}, 원인=워크스페이스 ID는 필수입니다", methodName);
            throw new WorkSpaceUserOperationException("워크스페이스 ID는 필수입니다");
        }
    }

    /**
     * 사용자-워크스페이스 관계 중복 검증
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     * @param methodName 호출한 메서드명 (로깅용)
     */
    private void validateNotExistingRelation(String userEmailId, UUID workspaceId, String methodName) {
        Optional<WorkSpaceUserEntity> existing = getWorkSpaceUserEntity(userEmailId, workspaceId);
        if (existing.isPresent()) {
            String message = String.format("이미 존재하는 사용자-워크스페이스 관계입니다: userEmailId=%s, workspaceId=%s", 
                                        userEmailId, workspaceId);
            log.warn("사용자-워크스페이스 관계 중복 검증 실패: 메서드={}, 원인={}", methodName, message);
            throw new WorkSpaceUserOperationException(message);
        }
    }

    /**
     * 사용자-워크스페이스 관계 존재 검증
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     * @param methodName 호출한 메서드명 (로깅용)
     * @return 존재하는 WorkSpaceUserEntity
     */
    private WorkSpaceUserEntity validateExistingRelation(String userEmailId, UUID workspaceId, String methodName) {
        Optional<WorkSpaceUserEntity> existing = getWorkSpaceUserEntity(userEmailId, workspaceId);
        if (existing.isEmpty()) {
            String message = String.format("사용자와 워크스페이스의 관계가 존재하지 않습니다: userEmailId=%s, workspaceId=%s", 
                                        userEmailId, workspaceId);
            log.warn("사용자-워크스페이스 관계 존재 검증 실패: 메서드={}, 원인={}", methodName, message);
            throw new WorkSpaceUserNotFoundException(message);
        }
        return existing.get();
    }

    /**
     * 사용자 권한 검증
     * @param currentUserEmail 현재 사용자 이메일
     * @param workspaceId 워크스페이스 ID
     * @param methodName 호출한 메서드명 (로깅용)
     */
    private void validateUserPermission(String currentUserEmail, UUID workspaceId, String methodName) {
        if (!canUserInviteAndDeleteUsers(currentUserEmail, workspaceId)) {
            String message = String.format("사용자 초대 및 삭제 권한이 없습니다: userEmailId=%s, workspaceId=%s", 
                                        currentUserEmail, workspaceId);
            log.warn("사용자 권한 검증 실패: 메서드={}, 원인={}", methodName, message);
            throw new WorkSpaceUserOperationException(message);
        }
    }

    /**
     * 사용자-워크스페이스 관계 조회
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     * @return WorkSpaceUserEntity (Optional)
     */
    private Optional<WorkSpaceUserEntity> getWorkSpaceUserEntity(String userEmailId, UUID workspaceId) {
        
        String methodName = "getWorkSpaceUserEntity";
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
            handleAndThrowWorkSpaceUserException(methodName, e);
            return null; // 실제로는 도달하지 않음
            
        }
    }


    /**
     * Role이 Admin, Status는 Active인 WorkSpaceUserEntity를 생성하는 Method
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     * @return 생성된 WorkSpaceUserEntity
     */
    @AuditLogging(action = "관리자_사용자_생성", includeParameters = true, includePerformance = true)
    public WorkSpaceUserEntity createAdminUserEntity(String userEmailId, UUID workspaceId) {
        String methodName = "createAdminUserEntity";
        log.info("[{}] Admin 사용자 생성 시작: userEmailId={}, workspaceId={}", methodName, userEmailId, workspaceId);
        
        try {
            // 1. 입력값 검증
            validateUserEmailId(userEmailId, methodName);
            validateWorkspaceId(workspaceId, methodName);
            validateNotExistingRelation(userEmailId, workspaceId, methodName);

            // 2. 종속 엔티티 조회
            WorkSpaceEntity workspace = workSpaceService.getWorkSpaceEntityOrThrow(workspaceId);
            AccountEntity user = accountService.getAccountEntityOrThrow(userEmailId);

            // 3. Admin으로 등록하는 WorkSpaceUserEntity 생성
            WorkSpaceUserEntity newUser = WorkSpaceUserFactory.createOwnerEntity(workspace, user);
            
            // 4. 저장 후 반환
            WorkSpaceUserEntity savedUser = workSpaceUserRepository.save(newUser);
            log.info("[{}] Admin 사용자 생성 완료: userEmailId={}, workspaceId={}, userId={}", 
                    methodName, userEmailId, workspaceId, savedUser.getId());
            
            return savedUser;

        } catch (WorkSpaceUserOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowWorkSpaceUserException(methodName, e);
            return null; // 실제로는 도달하지 않음
        }
    }


    /**
     * 특정 WorkSpaceUserEntity를 삭제하는 method
     * @param workspaceId 워크스페이스 ID
     * @param userEmailId 사용자 이메일 ID
     * @throws WorkSpaceUserNotFoundException 사용자-워크스페이스 관계가 존재하지 않을 때
     */
    @AuditLogging(action = "사용자_제거", includeParameters = true, includePerformance = true)
    @Transactional
    public void deleteWorkSpaceUserEntites(UUID workspaceId, String userEmailId) {
        String methodName = "deleteWorkSpaceUserEntites";
        log.info("[{}] 사용자 삭제 시작: workspaceId={}, userEmailId={}", methodName, workspaceId, userEmailId);
        
        try {
            // 1. 입력값 검증
            validateWorkspaceId(workspaceId, methodName);
            validateUserEmailId(userEmailId, methodName);

            // 2. 요청자 권한 검증
            String currentUserEmail = userInfoExtractor.getCurrentUserId();
            if (!currentUserEmail.equals(userEmailId)) {
                validateUserPermission(currentUserEmail, workspaceId, methodName);
            }

            // 3. 삭제 대상 관계 검증 및 조회
            WorkSpaceUserEntity entity = validateExistingRelation(userEmailId, workspaceId, methodName);
            
            // 4. 삭제 수행
            workSpaceUserRepository.delete(entity);
            
            log.info("[{}] 사용자 삭제 완료: workspaceId={}, userEmailId={}, entityId={}", 
                    methodName, workspaceId, userEmailId, entity.getId());

        } catch (WorkSpaceUserNotFoundException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (WorkSpaceUserOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowWorkSpaceUserException(methodName, e);
        }
    }

    /**
     * 해당 workspaceId의 WorkSpaceUserEntity를 모두 삭제하는 method
     * @param workspaceId 워크스페이스 ID
     */
    @Transactional
    public void deleteWorkSpaceUserAllEntites(UUID workspaceId) {
        String methodName = "deleteWorkSpaceUserAllEntites";
        log.info("[{}] 모든 사용자 삭제 시작: workspaceId={}", methodName, workspaceId);
        
        try {
            // 1. 입력값 검증
            validateWorkspaceId(workspaceId, methodName);

            // 2. 모든 사용자 삭제
            workSpaceUserRepository.deleteByWorkspace_Id(workspaceId);
            
            log.info("[{}] 모든 사용자 삭제 완료: workspaceId={}", methodName, workspaceId);
            
        } catch (WorkSpaceUserOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowWorkSpaceUserException(methodName, e);
        }
    }
    

    /**
     * WorkSpaceUserEntity의 Role을 Update 하는 Method
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     * @param newRole 새로운 워크스페이스 역할
     */
    @AuditLogging(action = "사용자_역할_변경", includeParameters = true, includePerformance = true)
    @Transactional
    public void updateUserRole(String userEmailId, UUID workspaceId, WorkSpaceRole newRole) {
        String methodName = "updateUserRole";
        log.info("[{}] 사용자 역할 업데이트 시작: userEmailId={}, workspaceId={}, newRole={}", 
                methodName, userEmailId, workspaceId, newRole);
        
        try {
            // 1. 입력값 검증
            validateUserEmailId(userEmailId, methodName);
            validateWorkspaceId(workspaceId, methodName);
            
            // 2. 권한 검증
            String currentUserEmail = userInfoExtractor.getCurrentUserId();
            validateUserPermission(currentUserEmail, workspaceId, methodName);

            // 3. 대상 사용자 존재 확인
            WorkSpaceUserEntity entity = validateExistingRelation(userEmailId, workspaceId, methodName);
            
            // 4. 현재 Role과 동일한 경우 업데이트 하지 않음
            if (entity.getWorkspaceRole() == newRole) {
                log.info("[{}] 사용자 역할이 이미 {}입니다. 업데이트하지 않습니다: userEmailId={}, workspaceId={}", 
                        methodName, newRole, userEmailId, workspaceId);
                return;
            }
            
            // 5. Role 업데이트
            entity.setWorkspaceRole(newRole);
            workSpaceUserRepository.save(entity);
            
            log.info("[{}] 사용자 역할 업데이트 완료: userEmailId={}, workspaceId={}, oldRole={}, newRole={}", 
                    methodName, userEmailId, workspaceId, entity.getWorkspaceRole(), newRole);

        } catch (WorkSpaceUserNotFoundException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (WorkSpaceUserOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowWorkSpaceUserException(methodName, e);
        }
    }



    /**
     * WorkSpaceUserEntity의 MembershipStatus Update 하는 Method
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     * @param newMembershipStatus 새로운 멤버십 상태
     */
    @Transactional
    public void updateMembershipStatus(String userEmailId, UUID workspaceId, MembershipStatus newMembershipStatus) {
        String methodName = "updateMembershipStatus";
        log.info("[{}] 멤버십 상태 업데이트 시작: userEmailId={}, workspaceId={}, newStatus={}", 
                methodName, userEmailId, workspaceId, newMembershipStatus);
        
        try {
            // 1. 입력값 검증
            validateUserEmailId(userEmailId, methodName);
            validateWorkspaceId(workspaceId, methodName);
            
            // 2. 대상 사용자 존재 확인
            WorkSpaceUserEntity entity = validateExistingRelation(userEmailId, workspaceId, methodName);
            
            // 3. 현재 상태와 동일한 경우 업데이트 하지 않음
            if (entity.getMembershipStatus() == newMembershipStatus) {
                log.info("[{}] 멤버십 상태가 이미 {}입니다. 업데이트하지 않습니다: userEmailId={}, workspaceId={}", 
                        methodName, newMembershipStatus, userEmailId, workspaceId);
                return;
            }
            
            // 4. 멤버십 상태 업데이트
            entity.setMembershipStatus(newMembershipStatus);
            workSpaceUserRepository.save(entity);
            
            log.info("[{}] 멤버십 상태 업데이트 완료: userEmailId={}, workspaceId={}, oldStatus={}, newStatus={}", 
                    methodName, userEmailId, workspaceId, entity.getMembershipStatus(), newMembershipStatus);

        } catch (WorkSpaceUserNotFoundException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (WorkSpaceUserOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowWorkSpaceUserException(methodName, e);
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
     * @return 생성된 WorkSpaceUserEntity
     */
    @AuditLogging(action = "관리자_초대", includeParameters = true, includePerformance = true)
    public WorkSpaceUserEntity inviteAsAdmin(String userEmailId, UUID workspaceId) {
        String methodName = "inviteAsAdmin";
        log.info("[{}] Admin 초대 시작: userEmailId={}, workspaceId={}", methodName, userEmailId, workspaceId);
        
        try {
            // 1. 입력값 검증
            validateUserEmailId(userEmailId, methodName);
            validateWorkspaceId(workspaceId, methodName);
            validateNotExistingRelation(userEmailId, workspaceId, methodName);

            // 2. 종속 엔티티 조회
            WorkSpaceEntity workspace = workSpaceService.getWorkSpaceEntityOrThrow(workspaceId);
            AccountEntity user = accountService.getAccountEntityOrThrow(userEmailId);

            // 3. Admin으로 초대하는 WorkSpaceUserEntity 생성
            WorkSpaceUserEntity invitedUser = WorkSpaceUserFactory.createdInvitedAdminEntity(workspace, user);
            
            // 4. 저장 후 반환
            WorkSpaceUserEntity savedUser = workSpaceUserRepository.save(invitedUser);
            log.info("[{}] Admin 초대 완료: userEmailId={}, workspaceId={}, userId={}", 
                    methodName, userEmailId, workspaceId, savedUser.getId());
            
            return savedUser;

        } catch (WorkSpaceUserOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowWorkSpaceUserException(methodName, e);
            return null; // 실제로는 도달하지 않음
        }
    }


    /**
     * Member로 초대하고 WorkSpaceUserEntity를 생성하는 Method
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     * @return 생성된 WorkSpaceUserEntity
     */
    @AuditLogging(action = "멤버_초대", includeParameters = true, includePerformance = true)
    public WorkSpaceUserEntity inviteAsMember(String userEmailId, UUID workspaceId) {
        String methodName = "inviteAsMember";
        log.info("[{}] Member 초대 시작: userEmailId={}, workspaceId={}", methodName, userEmailId, workspaceId);
        
        try {
            // 1. 입력값 검증
            validateUserEmailId(userEmailId, methodName);
            validateWorkspaceId(workspaceId, methodName);
            validateNotExistingRelation(userEmailId, workspaceId, methodName);

            // 2. 종속 엔티티 조회
            WorkSpaceEntity workspace = workSpaceService.getWorkSpaceEntityOrThrow(workspaceId);
            AccountEntity user = accountService.getAccountEntityOrThrow(userEmailId);

            // 3. Member로 초대하는 WorkSpaceUserEntity 생성
            WorkSpaceUserEntity invitedUser = WorkSpaceUserFactory.createdInvitedMemberEntity(workspace, user);
            
            // 4. 저장 후 반환
            WorkSpaceUserEntity savedUser = workSpaceUserRepository.save(invitedUser);
            log.info("[{}] Member 초대 완료: userEmailId={}, workspaceId={}, userId={}", 
                    methodName, userEmailId, workspaceId, savedUser.getId());
            
            return savedUser;

        } catch (WorkSpaceUserOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowWorkSpaceUserException(methodName, e);
            return null; // 실제로는 도달하지 않음
        }
    }

    /**
     * Guest로 초대하고 WorkSpaceUserEntity를 생성하는 Method
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     * @return 생성된 WorkSpaceUserEntity
     */
    @AuditLogging(action = "게스트_초대", includeParameters = true, includePerformance = true)
    public WorkSpaceUserEntity inviteAsGuest(String userEmailId, UUID workspaceId) {
        String methodName = "inviteAsGuest";
        log.info("[{}] Guest 초대 시작: userEmailId={}, workspaceId={}", methodName, userEmailId, workspaceId);
        
        try {
            // 1. 입력값 검증
            validateUserEmailId(userEmailId, methodName);
            validateWorkspaceId(workspaceId, methodName);
            validateNotExistingRelation(userEmailId, workspaceId, methodName);

            // 2. 종속 엔티티 조회
            WorkSpaceEntity workspace = workSpaceService.getWorkSpaceEntityOrThrow(workspaceId);
            AccountEntity user = accountService.getAccountEntityOrThrow(userEmailId);

            // 3. Guest로 초대하는 WorkSpaceUserEntity 생성
            WorkSpaceUserEntity invitedUser = WorkSpaceUserFactory.createdInvitedGuestEntity(workspace, user);
            
            // 4. 저장 후 반환
            WorkSpaceUserEntity savedUser = workSpaceUserRepository.save(invitedUser);
            log.info("[{}] Guest 초대 완료: userEmailId={}, workspaceId={}, userId={}", 
                    methodName, userEmailId, workspaceId, savedUser.getId());
            
            return savedUser;

        } catch (WorkSpaceUserOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowWorkSpaceUserException(methodName, e);
            return null; // 실제로는 도달하지 않음
        }
    }


    /**
     * 사용자가 워크스페이스를 관리할 수 있는지 확인
     * @param userEmailId 사용자 이메일 ID
     * @param workspaceId 워크스페이스 ID
     * @return 관리 권한 여부
     */
    public boolean canUserManageWorkSpace(String userEmailId, UUID workspaceId) {
        
        return checkUserPermission(userEmailId, workspaceId, "manage workspace", 
                entity -> entity.getWorkspaceRole().canManageWorkspace());
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
        String methodName = "checkUserPermission";
        
        try {
            log.debug("[{}] {} 권한 확인: userEmailId={}, workspaceId={}", 
                    methodName, permissionName, userEmailId, workspaceId);
        
            // 입력값 검증
            validateUserEmailId(userEmailId, methodName);
            validateWorkspaceId(workspaceId, methodName);

            // 사용자-워크스페이스 관계 조회
            Optional<WorkSpaceUserEntity> result = getWorkSpaceUserEntity(userEmailId, workspaceId);        
            // 사용자-워크스페이스 관계가 존재하지 않음
            if (result.isEmpty()) {
                // 🔐 보안 이벤트: 관계 없는 사용자의 접근 시도
                log.warn("[{}] 권한 없는 {} 시도 - 사용자 관계 없음: userEmailId={}, workspaceId={}", 
                        methodName, permissionName, userEmailId, workspaceId);
                return false;
            }

            boolean hasPermission = result.filter(entity -> entity.getMembershipStatus() == MembershipStatus.ACTIVE)
                                        .map(permissionChecker)
                                        .orElse(false);
            
            // 권한 결과에 따른 로깅
            if (!hasPermission) {
                // 🔐 보안 이벤트: 권한 부족으로 인한 거부
                log.warn("[{}] 권한 부족으로 인한 {} 거부: userEmailId={}, workspaceId={}, " +
                        "membershipStatus={}, role={}", 
                        methodName, permissionName, userEmailId, workspaceId,
                        result.get().getMembershipStatus(),
                        result.get().getWorkspaceRole());
            }
            else {
                log.info("[{}] {} 권한 확인 성공: userEmailId={}, workspaceId={}, " +
                        "membershipStatus={}, role={}", 
                        methodName, permissionName, userEmailId, workspaceId,
                        result.get().getMembershipStatus(),
                        result.get().getWorkspaceRole());
            }
            
            return hasPermission;
            
        } catch (WorkSpaceUserOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            return false;
        } 
        catch (BusinessException e)
        {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            return false;
        } 
        catch (Exception e) {
            handleAndThrowWorkSpaceUserException(methodName, e);
            return false; // 실제로는 도달하지 않음
        }
    }


    /**
     * WorkSpaceUser 공통 예외 처리 메서드
     * 예외 타입에 따라 자동으로 warn/error logging 결정
     * @param methodName 호출한 메서드명 (로깅용)
     * @param originalException 원본 예외
     * @throws WorkSpaceUserOperationException Wrapped 예외
     */
        private void handleAndThrowWorkSpaceUserException(String methodName, Exception originalException) {
        WorkSpaceUserOperationException customException = new WorkSpaceUserOperationException(
            String.format("%s 실패 [%s]: %s", methodName, 
                         originalException.getClass().getSimpleName(), 
                         originalException.getMessage()),
            originalException
        );
        
        // BaseService의 메서드를 사용하여 예외 타입에 따라 warn/error 로깅
        handleAndThrow(methodName, originalException, customException);
    }

}

