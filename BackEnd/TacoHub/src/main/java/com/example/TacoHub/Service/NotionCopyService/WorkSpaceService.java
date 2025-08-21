package com.example.TacoHub.Service.NotionCopyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.TacoHub.Converter.NotionCopyConveter.WorkSpaceConverter;
import com.example.TacoHub.Dto.NotionCopyDTO.WorkSpaceDTO;
import com.example.TacoHub.Entity.NotionCopyEntity.PageEntity;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceEntity;
import com.example.TacoHub.Exception.BusinessException;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceNotFoundException;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceOperationException;
import com.example.TacoHub.Logging.AuditLogging;
import com.example.TacoHub.Logging.UserInfoExtractor;
import com.example.TacoHub.Repository.NotionCopyRepository.WorkSpaceRepository;
import com.example.TacoHub.Service.BaseService;

import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkSpaceService extends BaseService {

    private final WorkSpaceRepository workspaceRepository;
    private final PageService pageService;
    private final WorkSpaceUserService workSpaceUserService;
    private final UserInfoExtractor userInfoExtractor;

    // ===== 입력값 검증 메서드 =====
    
    /**
     * 워크스페이스 ID 검증
     * @param workspaceId 검증할 워크스페이스 ID
     * @param methodName 호출한 메서드명 (로깅용)
     */
    private void validateWorkspaceId(UUID workspaceId, String methodName) {
        if (isNull(workspaceId)) {
            log.warn("워크스페이스 ID 검증 실패: 메서드={}, 원인=워크스페이스 ID는 필수입니다", methodName);
            throw new WorkSpaceOperationException("워크스페이스 ID는 필수입니다");
        }
    }

    /**
     * 워크스페이스 이름 검증
     * @param workspaceName 검증할 워크스페이스 이름
     * @param methodName 호출한 메서드명 (로깅용)
     */
    private void validateWorkspaceName(String workspaceName, String methodName) {
        if (isStringNullOrEmpty(workspaceName)) {
            log.warn("워크스페이스 이름 검증 실패: 메서드={}, 원인=워크스페이스 이름은 필수입니다", methodName);
            throw new WorkSpaceOperationException("워크스페이스 이름은 필수입니다");
        }
        
        if (isStringTooLong(workspaceName, 100)) {
            log.warn("워크스페이스 이름 검증 실패: 메서드={}, 원인=워크스페이스 이름은 100자를 초과할 수 없습니다, length={}", 
                    methodName, workspaceName.length());
            throw new WorkSpaceOperationException("워크스페이스 이름은 100자를 초과할 수 없습니다");
        }
    }

    /**
     * 현재 사용자 이메일 검증
     * @param methodName 호출한 메서드명 (로깅용)
     * @return 검증된 사용자 이메일
     */
    private String validateCurrentUserEmail(String methodName) {
        String currentUserEmail = userInfoExtractor.getCurrentUserEmail();
        if (isStringNullOrEmpty(currentUserEmail)) {
            log.warn("현재 사용자 이메일 검증 실패: 메서드={}, 원인=현재 사용자 이메일을 확인할 수 없습니다", methodName);
            throw new WorkSpaceOperationException("현재 사용자 이메일을 확인할 수 없습니다");
        }
        return currentUserEmail;
    }

    /**
     * 워크스페이스 관리 권한 검증
     * @param userEmail 사용자 이메일
     * @param workspaceId 워크스페이스 ID
     * @param methodName 호출한 메서드명 (로깅용)
     */
    private void validateWorkspaceManagePermission(String userEmail, UUID workspaceId, String methodName) {
        if (!workSpaceUserService.canUserManageWorkSpace(userEmail, workspaceId)) {
            log.warn("워크스페이스 관리 권한 검증 실패: 메서드={}, 원인=워크스페이스 관리 권한이 없습니다, userEmail={}, workspaceId={}", 
                    methodName, userEmail, workspaceId);
            throw new WorkSpaceOperationException("워크스페이스 관리 권한이 없습니다");
        }
    }

    /**
     * 새로운 워크스페이스를 생성합니다
     * @param newWorkspaceName 생성할 워크스페이스의 이름
     * @return 생성된 워크스페이스의 Entity
     */
    @AuditLogging(action = "워크스페이스_생성", includeParameters = true, includePerformance = true)
    @Transactional
    public WorkSpaceEntity createWorkspaceEntity(String newWorkspaceName) {
        String methodName = "createWorkspaceEntity";
        log.info("[{}] 워크스페이스 생성 시작: name={}", methodName, newWorkspaceName);
        
        try {
            // 1. 입력값 검증
            validateWorkspaceName(newWorkspaceName, methodName);

            // 1.5 요청 사용자 검증
            String currentUserEmail = validateCurrentUserEmail(methodName);

            // 2. 기초 entity 생성
            WorkSpaceEntity newWorkSpace = WorkSpaceEntity.builder()
                .name(newWorkspaceName.trim())
                .build();

            WorkSpaceEntity savedEntity = workspaceRepository.save(newWorkSpace);

            // 3. 초기 default 페이지 생성
            PageEntity defaultRootPage = pageService.createPageEntity(savedEntity.getId());

            // 4. 워크스페이스와 페이지 연관 설정
            savedEntity.getRootPages().add(defaultRootPage);
            WorkSpaceEntity updatedEntity = workspaceRepository.save(savedEntity);

            // 5. 워크스페이스 사용자 생성 (관리자)
            workSpaceUserService.createAdminUserEntity(currentUserEmail, updatedEntity.getId());

            log.info("[{}] 워크스페이스 생성 완료: id={}, name={}", methodName, updatedEntity.getId(), updatedEntity.getName());
            return updatedEntity;

        } catch (WorkSpaceOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (WorkSpaceNotFoundException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowWorkSpaceException(methodName, e);       
            return null; // 실제로는 도달하지 않음
        }
    }

    /**
     * 워크스페이스의 이름을 변경합니다
     * @param newWorkspaceName 새로운 워크스페이스 이름
     * @param workspaceId 변경할 워크스페이스의 ID
     */
    @AuditLogging(action = "워크스페이스_이름_변경", includeParameters = true, includePerformance = true)
    @Transactional
    public void editWorkspaceName(String newWorkspaceName, UUID workspaceId) {
        String methodName = "editWorkspaceName";
        log.info("[{}] 워크스페이스 이름 변경 시작: id={}, newName={}", methodName, workspaceId, newWorkspaceName);
        
        try {
            // 1. 입력값 검증
            validateWorkspaceId(workspaceId, methodName);
            validateWorkspaceName(newWorkspaceName, methodName);

            // 2. 워크스페이스 조회 (존재하지 않으면 예외 발생)
            WorkSpaceEntity workspace = getWorkSpaceEntityOrThrow(workspaceId);

            // 3. 워크스페이스 이름 변경
            workspace.setName(newWorkspaceName.trim());

            // 4. 변경된 워크스페이스 저장
            workspaceRepository.save(workspace);
            
            log.info("[{}] 워크스페이스 이름 변경 완료: id={}, newName={}", methodName, workspaceId, newWorkspaceName);

        } catch (WorkSpaceOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (WorkSpaceNotFoundException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowWorkSpaceException(methodName, e);       
        }
    }

    /**
     * 워크스페이스를 삭제합니다
     * @param workspaceId 삭제할 워크스페이스의 ID
     */
    @Transactional
    public void deleteWorkspace(UUID workspaceId) {
        String methodName = "deleteWorkspace";
        log.info("[{}] 워크스페이스 삭제 시작: id={}", methodName, workspaceId);
        
        try {
            // 1. 입력값 검증
            validateWorkspaceId(workspaceId, methodName);

            // 2. 워크스페이스 존재 확인
            if (!workspaceRepository.existsById(workspaceId)) {
                log.warn("워크스페이스 존재 확인 실패: 메서드={}, 원인=워크스페이스가 존재하지 않습니다, workspaceId={}", 
                        methodName, workspaceId);
                throw new WorkSpaceNotFoundException("워크스페이스가 존재하지 않습니다: " + workspaceId);
            }

            // 3. 삭제 권한 검증
            String currentUserEmail = validateCurrentUserEmail(methodName);
            validateWorkspaceManagePermission(currentUserEmail, workspaceId, methodName);

            // 4. 관련 데이터 삭제
            pageService.deletePageEntityByWorkspaceId(workspaceId);
            workSpaceUserService.deleteWorkSpaceUserAllEntites(workspaceId);

            // 5. 워크스페이스 삭제
            workspaceRepository.deleteById(workspaceId);
            
            log.info("[{}] 워크스페이스 삭제 완료: id={}", methodName, workspaceId);

        } catch (WorkSpaceOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (WorkSpaceNotFoundException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowWorkSpaceException(methodName, e);       
        }
    }

    /**
     * 워크스페이스 정보를 조회합니다
     * @param workspaceId 조회할 워크스페이스의 ID
     * @return 조회된 워크스페이스의 DTO
     */
    public WorkSpaceDTO getWorkspaceDto(UUID workspaceId) {
        String methodName = "getWorkspaceDto";

        try {
            log.info("워크스페이스 조회 시작: id={}", workspaceId);
            
            // 1. 입력값 검증
            validateWorkspaceId(workspaceId, methodName);
            

            // 워크스페이스 조회 (존재하지 않으면 예외 발생)
            WorkSpaceEntity workspace = getWorkSpaceEntityOrThrow(workspaceId);

            WorkSpaceDTO dto = WorkSpaceConverter.toDTO(workspace);

            log.info("워크스페이스 조회 완료: id={}", workspaceId);
            return dto;

        } catch (WorkSpaceNotFoundException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch(WorkSpaceOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e)
        {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;        
        }
        catch (Exception e) {
            // 예상치 못한 예외는 래핑해서 전파
            handleAndThrowWorkSpaceException("getWorkspace", e);
            return null; // 실제로는 도달하지 않음
        }
    }

    /**
     * WorkSpaceEntity를 ID로 조회하고, 없으면 예외를 던지는 메서드
     * @param workspaceId 조회할 워크스페이스의 Id
     * @return 조회된 WorkSpaceEntity
     * @throws WorkSpaceNotFoundException 워크스페이스가 존재하지 않을 때
     */
    @AuditLogging(action = "워크스페이스_조회", includeParameters = true)
    public WorkSpaceEntity getWorkSpaceEntityOrThrow(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> {
                    log.warn("워크스페이스 조회 실패: ID가 존재하지 않음, id={}", workspaceId);
                    return new WorkSpaceNotFoundException("워크스페이스가 존재하지 않습니다: " + workspaceId);
                });
    }

    /**
     * 공통 WorkSpace 예외 처리 메서드
     * 예외 타입에 따라 자동으로 warn/error 로깅을 결정
     * 
     * @param methodName 실패한 메서드명
     * @param originalException 원본 예외
     * @throws WorkSpaceOperationException 래핑된 예외
     */
    private void handleAndThrowWorkSpaceException(String methodName, Exception originalException) {
        WorkSpaceOperationException customException = new WorkSpaceOperationException(
            String.format("%s 실패 [%s]: %s", methodName, 
                        originalException.getClass().getSimpleName(), 
                        originalException.getMessage()),
            originalException
        );
        
        // BaseService의 메서드를 사용하여 예외 타입에 따라 warn/error 로깅
        handleAndThrow(methodName, originalException, customException);
    }



}
